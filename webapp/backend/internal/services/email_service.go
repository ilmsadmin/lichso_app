package services

import (
	"bytes"
	"crypto/tls"
	"fmt"
	"html/template"
	"net/smtp"
	"strings"

	"github.com/zplus/lichso/internal/config"
	"go.uber.org/zap"
)

// EmailService handles sending emails via SMTP
type EmailService struct {
	cfg    *config.SMTPConfig
	appCfg *config.AppConfig
	logger *zap.Logger
}

// NewEmailService creates a new EmailService
func NewEmailService(cfg *config.SMTPConfig, appCfg *config.AppConfig, logger *zap.Logger) *EmailService {
	return &EmailService{
		cfg:    cfg,
		appCfg: appCfg,
		logger: logger,
	}
}

// IsEnabled returns whether email sending is enabled
func (s *EmailService) IsEnabled() bool {
	return s.cfg.Enabled && s.cfg.Host != "" && s.cfg.From != ""
}

// SendEmail sends an email with the given subject and HTML body
func (s *EmailService) SendEmail(to, subject, htmlBody string) error {
	if !s.IsEnabled() {
		s.logger.Warn("Email sending is disabled, skipping",
			zap.String("to", to),
			zap.String("subject", subject),
		)
		return nil
	}

	from := s.cfg.From
	if s.cfg.FromName != "" {
		from = fmt.Sprintf("%s <%s>", s.cfg.FromName, s.cfg.From)
	}

	// Build email headers and body
	msg := strings.Builder{}
	msg.WriteString(fmt.Sprintf("From: %s\r\n", from))
	msg.WriteString(fmt.Sprintf("To: %s\r\n", to))
	msg.WriteString(fmt.Sprintf("Subject: %s\r\n", subject))
	msg.WriteString("MIME-Version: 1.0\r\n")
	msg.WriteString("Content-Type: text/html; charset=\"UTF-8\"\r\n")
	msg.WriteString("\r\n")
	msg.WriteString(htmlBody)

	addr := fmt.Sprintf("%s:%d", s.cfg.Host, s.cfg.Port)

	var auth smtp.Auth
	if s.cfg.User != "" {
		auth = smtp.PlainAuth("", s.cfg.User, s.cfg.Password, s.cfg.Host)
	}

	var err error
	switch s.cfg.Encryption {
	case "ssl":
		err = s.sendWithSSL(addr, auth, s.cfg.From, to, []byte(msg.String()))
	case "tls":
		err = s.sendWithTLS(addr, auth, s.cfg.From, to, []byte(msg.String()))
	default:
		err = smtp.SendMail(addr, auth, s.cfg.From, []string{to}, []byte(msg.String()))
	}

	if err != nil {
		s.logger.Error("Failed to send email",
			zap.String("to", to),
			zap.String("subject", subject),
			zap.Error(err),
		)
		return fmt.Errorf("failed to send email: %w", err)
	}

	s.logger.Info("Email sent successfully",
		zap.String("to", to),
		zap.String("subject", subject),
	)
	return nil
}

// sendWithTLS sends email using STARTTLS
func (s *EmailService) sendWithTLS(addr string, auth smtp.Auth, from, to string, msg []byte) error {
	conn, err := smtp.Dial(addr)
	if err != nil {
		return err
	}
	defer conn.Close()

	tlsConfig := &tls.Config{
		ServerName: s.cfg.Host,
	}

	if err := conn.StartTLS(tlsConfig); err != nil {
		return err
	}

	if auth != nil {
		if err := conn.Auth(auth); err != nil {
			return err
		}
	}

	if err := conn.Mail(from); err != nil {
		return err
	}
	if err := conn.Rcpt(to); err != nil {
		return err
	}

	w, err := conn.Data()
	if err != nil {
		return err
	}

	if _, err := w.Write(msg); err != nil {
		return err
	}
	if err := w.Close(); err != nil {
		return err
	}

	return conn.Quit()
}

// sendWithSSL sends email using direct SSL/TLS connection
func (s *EmailService) sendWithSSL(addr string, auth smtp.Auth, from, to string, msg []byte) error {
	tlsConfig := &tls.Config{
		ServerName: s.cfg.Host,
	}

	conn, err := tls.Dial("tcp", addr, tlsConfig)
	if err != nil {
		return err
	}
	defer conn.Close()

	client, err := smtp.NewClient(conn, s.cfg.Host)
	if err != nil {
		return err
	}
	defer client.Close()

	if auth != nil {
		if err := client.Auth(auth); err != nil {
			return err
		}
	}

	if err := client.Mail(from); err != nil {
		return err
	}
	if err := client.Rcpt(to); err != nil {
		return err
	}

	w, err := client.Data()
	if err != nil {
		return err
	}
	if _, err := w.Write(msg); err != nil {
		return err
	}
	if err := w.Close(); err != nil {
		return err
	}

	return client.Quit()
}

// ============================================
// Email Templates
// ============================================

// SendPasswordResetEmail sends a password reset email
func (s *EmailService) SendPasswordResetEmail(to, userName, resetToken string) error {
	resetURL := fmt.Sprintf("%s/reset-password?token=%s", s.appCfg.URL, resetToken)

	data := map[string]string{
		"AppName":  s.appCfg.Name,
		"UserName": userName,
		"ResetURL": resetURL,
		"AppURL":   s.appCfg.URL,
	}

	body, err := s.renderTemplate(passwordResetTemplate, data)
	if err != nil {
		return err
	}

	return s.SendEmail(to, fmt.Sprintf("[%s] Password Reset Request", s.appCfg.Name), body)
}

// SendWelcomeEmail sends a welcome email to new users
func (s *EmailService) SendWelcomeEmail(to, userName string) error {
	data := map[string]string{
		"AppName":  s.appCfg.Name,
		"UserName": userName,
		"LoginURL": fmt.Sprintf("%s/login", s.appCfg.URL),
		"AppURL":   s.appCfg.URL,
	}

	body, err := s.renderTemplate(welcomeEmailTemplate, data)
	if err != nil {
		return err
	}

	return s.SendEmail(to, fmt.Sprintf("Welcome to %s!", s.appCfg.Name), body)
}

// SendNotificationEmail sends an email notification about an in-app notification
func (s *EmailService) SendNotificationEmail(to, userName, title, message, notifType string, link ...string) error {
	notifLink := ""
	if len(link) > 0 && link[0] != "" {
		notifLink = link[0]
		if !strings.HasPrefix(notifLink, "http") {
			notifLink = fmt.Sprintf("%s%s", s.appCfg.URL, notifLink)
		}
	}

	data := map[string]string{
		"AppName":      s.appCfg.Name,
		"UserName":     userName,
		"Title":        title,
		"Message":      message,
		"Type":         notifType,
		"Link":         notifLink,
		"AppURL":       s.appCfg.URL,
		"DashboardURL": fmt.Sprintf("%s/admin", s.appCfg.URL),
	}

	body, err := s.renderTemplate(notificationEmailTemplate, data)
	if err != nil {
		return err
	}

	return s.SendEmail(to, fmt.Sprintf("[%s] %s", s.appCfg.Name, title), body)
}

// SendAccountStatusEmail sends an email when account status changes
func (s *EmailService) SendAccountStatusEmail(to, userName string, isActive bool) error {
	status := "Activated"
	if !isActive {
		status = "Deactivated"
	}

	data := map[string]string{
		"AppName":  s.appCfg.Name,
		"UserName": userName,
		"Status":   status,
		"IsActive": fmt.Sprintf("%t", isActive),
		"AppURL":   s.appCfg.URL,
	}

	body, err := s.renderTemplate(accountStatusTemplate, data)
	if err != nil {
		return err
	}

	return s.SendEmail(to, fmt.Sprintf("[%s] Account %s", s.appCfg.Name, status), body)
}

// SendTestEmail sends a test email to verify SMTP configuration
func (s *EmailService) SendTestEmail(to string) error {
	data := map[string]string{
		"AppName": s.appCfg.Name,
		"AppURL":  s.appCfg.URL,
	}

	body, err := s.renderTemplate(testEmailTemplate, data)
	if err != nil {
		return err
	}

	return s.SendEmail(to, fmt.Sprintf("[%s] Test Email", s.appCfg.Name), body)
}

// SendReminderEmail sends a reminder notification email to the user
func (s *EmailService) SendReminderEmail(to, userName, title, description, reminderTypeLabel, dateLabel string, isLunar bool) error {
	data := map[string]interface{}{
		"AppName":           s.appCfg.Name,
		"AppURL":            s.appCfg.URL,
		"UserName":          userName,
		"Title":             title,
		"Description":       description,
		"ReminderTypeLabel": reminderTypeLabel,
		"DateLabel":         dateLabel,
		"IsLunar":           isLunar,
	}

	body, err := s.renderTemplateAny(reminderEmailTemplate, data)
	if err != nil {
		return err
	}

	subject := fmt.Sprintf("[%s] Nhắc nhở: %s", s.appCfg.Name, title)
	return s.SendEmail(to, subject, body)
}

// renderTemplate renders an HTML template with string data
func (s *EmailService) renderTemplate(tmpl string, data map[string]string) (string, error) {
	t, err := template.New("email").Parse(tmpl)
	if err != nil {
		return "", fmt.Errorf("failed to parse email template: %w", err)
	}

	var buf bytes.Buffer
	if err := t.Execute(&buf, data); err != nil {
		return "", fmt.Errorf("failed to execute email template: %w", err)
	}

	return buf.String(), nil
}

// renderTemplateAny renders an HTML template with mixed-type data (supports bool, string, etc.)
func (s *EmailService) renderTemplateAny(tmpl string, data map[string]interface{}) (string, error) {
	t, err := template.New("email").Parse(tmpl)
	if err != nil {
		return "", fmt.Errorf("failed to parse email template: %w", err)
	}

	var buf bytes.Buffer
	if err := t.Execute(&buf, data); err != nil {
		return "", fmt.Errorf("failed to execute email template: %w", err)
	}

	return buf.String(), nil
}
