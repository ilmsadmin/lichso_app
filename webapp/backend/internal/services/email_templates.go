package services

// ============================================
// Email HTML Templates
// ============================================

const emailBaseStyle = `
<style>
  body { margin: 0; padding: 0; background-color: #f4f7fa; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
  .container { max-width: 600px; margin: 0 auto; padding: 40px 20px; }
  .card { background-color: #ffffff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); overflow: hidden; }
  .header { background: linear-gradient(135deg, #6366f1, #8b5cf6); padding: 32px 40px; text-align: center; }
  .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; }
  .body { padding: 40px; }
  .body h2 { color: #1f2937; margin: 0 0 16px 0; font-size: 20px; }
  .body p { color: #4b5563; margin: 0 0 16px 0; line-height: 1.6; font-size: 15px; }
  .btn { display: inline-block; background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #ffffff !important; padding: 14px 32px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 15px; margin: 8px 0; }
  .btn:hover { opacity: 0.9; }
  .footer { text-align: center; padding: 24px 40px; border-top: 1px solid #e5e7eb; }
  .footer p { color: #9ca3af; font-size: 13px; margin: 0; }
  .divider { height: 1px; background-color: #e5e7eb; margin: 24px 0; }
  .badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; text-transform: uppercase; }
  .badge-info { background-color: #dbeafe; color: #1d4ed8; }
  .badge-success { background-color: #dcfce7; color: #15803d; }
  .badge-warning { background-color: #fef3c7; color: #b45309; }
  .badge-error { background-color: #fee2e2; color: #b91c1c; }
  .text-muted { color: #9ca3af; font-size: 13px; }
</style>
`

// passwordResetTemplate is the HTML template for password reset emails
const passwordResetTemplate = `<!DOCTYPE html>
<html>
<head><meta charset="UTF-8">` + emailBaseStyle + `</head>
<body>
<div class="container">
  <div class="card">
    <div class="header">
      <h1>{{.AppName}}</h1>
    </div>
    <div class="body">
      <h2>Password Reset Request</h2>
      <p>Hi {{.UserName}},</p>
      <p>We received a request to reset your password. Click the button below to set a new password:</p>
      <p style="text-align: center; margin: 32px 0;">
        <a href="{{.ResetURL}}" class="btn">Reset Password</a>
      </p>
      <p class="text-muted">This link will expire in 1 hour. If you didn't request a password reset, you can safely ignore this email.</p>
      <div class="divider"></div>
      <p class="text-muted">If the button doesn't work, copy and paste this link into your browser:</p>
      <p class="text-muted" style="word-break: break-all;">{{.ResetURL}}</p>
    </div>
    <div class="footer">
      <p>&copy; {{.AppName}} &middot; <a href="{{.AppURL}}" style="color: #6366f1;">{{.AppURL}}</a></p>
    </div>
  </div>
</div>
</body>
</html>`

// welcomeEmailTemplate is the HTML template for welcome emails
const welcomeEmailTemplate = `<!DOCTYPE html>
<html>
<head><meta charset="UTF-8">` + emailBaseStyle + `</head>
<body>
<div class="container">
  <div class="card">
    <div class="header">
      <h1>{{.AppName}}</h1>
    </div>
    <div class="body">
      <h2>Welcome! 🎉</h2>
      <p>Hi {{.UserName}},</p>
      <p>Your account has been successfully created. You can now log in to access the admin dashboard.</p>
      <p style="text-align: center; margin: 32px 0;">
        <a href="{{.LoginURL}}" class="btn">Go to Dashboard</a>
      </p>
      <p>If you have any questions, feel free to reach out to our support team.</p>
    </div>
    <div class="footer">
      <p>&copy; {{.AppName}} &middot; <a href="{{.AppURL}}" style="color: #6366f1;">{{.AppURL}}</a></p>
    </div>
  </div>
</div>
</body>
</html>`

// notificationEmailTemplate is the HTML template for notification emails
const notificationEmailTemplate = `<!DOCTYPE html>
<html>
<head><meta charset="UTF-8">` + emailBaseStyle + `</head>
<body>
<div class="container">
  <div class="card">
    <div class="header">
      <h1>{{.AppName}}</h1>
    </div>
    <div class="body">
      <p>Hi {{.UserName}},</p>
      <p>You have a new notification:</p>
      <div style="background-color: #f9fafb; border-radius: 8px; padding: 20px; margin: 16px 0;">
        <p style="margin: 0 0 8px 0;">
          <span class="badge badge-{{.Type}}">{{.Type}}</span>
        </p>
        <h2 style="margin: 8px 0;">{{.Title}}</h2>
        <p style="margin: 0;">{{.Message}}</p>
      </div>
      {{if .Link}}
      <p style="text-align: center; margin: 24px 0;">
        <a href="{{.Link}}" class="btn">View Details</a>
      </p>
      {{end}}
      <p style="text-align: center;">
        <a href="{{.DashboardURL}}" style="color: #6366f1; text-decoration: none; font-size: 14px;">Go to Dashboard →</a>
      </p>
    </div>
    <div class="footer">
      <p>&copy; {{.AppName}} &middot; <a href="{{.AppURL}}" style="color: #6366f1;">{{.AppURL}}</a></p>
    </div>
  </div>
</div>
</body>
</html>`

// accountStatusTemplate is the HTML template for account status change emails
const accountStatusTemplate = `<!DOCTYPE html>
<html>
<head><meta charset="UTF-8">` + emailBaseStyle + `</head>
<body>
<div class="container">
  <div class="card">
    <div class="header">
      <h1>{{.AppName}}</h1>
    </div>
    <div class="body">
      <h2>Account {{.Status}}</h2>
      <p>Hi {{.UserName}},</p>
      {{if eq .IsActive "true"}}
      <p>Your account has been <strong>activated</strong>. You can now log in and access the platform.</p>
      <p style="text-align: center; margin: 32px 0;">
        <a href="{{.AppURL}}/login" class="btn">Log In Now</a>
      </p>
      {{else}}
      <p>Your account has been <strong>deactivated</strong>. If you believe this is an error, please contact the administrator.</p>
      {{end}}
    </div>
    <div class="footer">
      <p>&copy; {{.AppName}} &middot; <a href="{{.AppURL}}" style="color: #6366f1;">{{.AppURL}}</a></p>
    </div>
  </div>
</div>
</body>
</html>`

// testEmailTemplate is the HTML template for test emails
const testEmailTemplate = `<!DOCTYPE html>
<html>
<head><meta charset="UTF-8">` + emailBaseStyle + `</head>
<body>
<div class="container">
  <div class="card">
    <div class="header">
      <h1>{{.AppName}}</h1>
    </div>
    <div class="body">
      <h2>Test Email ✅</h2>
      <p>This is a test email from <strong>{{.AppName}}</strong>.</p>
      <p>If you received this email, your SMTP configuration is working correctly!</p>
      <div class="divider"></div>
      <p class="text-muted">This email was sent as a test from the admin settings panel.</p>
    </div>
    <div class="footer">
      <p>&copy; {{.AppName}} &middot; <a href="{{.AppURL}}" style="color: #6366f1;">{{.AppURL}}</a></p>
    </div>
  </div>
</div>
</body>
</html>`

// reminderEmailTemplate is the HTML template for reminder notification emails (Vietnamese)
const reminderEmailTemplate = `<!DOCTYPE html>
<html>
<head><meta charset="UTF-8">
<style>
  body { margin: 0; padding: 0; background-color: #fdf6ec; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
  .container { max-width: 600px; margin: 0 auto; padding: 40px 20px; }
  .card { background-color: #fffdf7; border-radius: 16px; overflow: hidden; border: 1px solid rgba(200,144,42,0.2); box-shadow: 0 4px 24px rgba(196,120,58,0.10); }
  .header { background: linear-gradient(135deg, #c4783a 0%, #a85c20 100%); padding: 32px 40px; text-align: center; }
  .header h1 { color: #fff8ee; margin: 0; font-size: 22px; font-weight: 700; letter-spacing: 1px; }
  .header .subtitle { color: rgba(255,248,238,0.8); font-size: 13px; margin-top: 4px; }
  .body { padding: 36px 40px; }
  .greeting { color: #7a5c3a; font-size: 15px; margin-bottom: 20px; }
  .reminder-box { background: rgba(200,144,42,0.07); border: 1px solid rgba(200,144,42,0.22); border-radius: 12px; padding: 24px; margin: 20px 0; }
  .reminder-type { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; background: rgba(200,144,42,0.15); color: #a07030; margin-bottom: 10px; }
  .reminder-title { color: #2d1a06; font-size: 20px; font-weight: 700; margin: 0 0 8px 0; }
  .reminder-desc { color: #7a5c3a; font-size: 14px; line-height: 1.6; margin: 0; }
  .date-info { display: flex; gap: 12px; margin-top: 16px; flex-wrap: wrap; }
  .date-chip { background: rgba(61,128,112,0.09); border: 1px solid rgba(61,128,112,0.2); border-radius: 8px; padding: 6px 14px; font-size: 13px; color: #3d8070; font-weight: 500; }
  .btn { display: inline-block; background: linear-gradient(135deg, #c4783a, #a85c20); color: #fff8ee !important; padding: 13px 32px; text-decoration: none; border-radius: 10px; font-weight: 600; font-size: 14px; margin: 24px 0 8px 0; }
  .divider { height: 1px; background: linear-gradient(90deg, transparent, rgba(200,144,42,0.25), transparent); margin: 24px 0; }
  .footer { text-align: center; padding: 20px 40px; }
  .footer p { color: #a07850; font-size: 12px; margin: 0; }
  .lotus { font-size: 28px; display: block; text-align: center; margin-bottom: 8px; }
</style>
</head>
<body>
<div class="container">
  <div class="card">
    <div class="header">
      <span class="lotus">🌸</span>
      <h1>{{.AppName}}</h1>
      <div class="subtitle">Nhắc nhở của bạn</div>
    </div>
    <div class="body">
      <p class="greeting">Xin chào, <strong>{{.UserName}}</strong> 👋</p>
      <p style="color:#7a5c3a;font-size:14px;margin-bottom:20px;">
        Bạn có một nhắc nhở quan trọng vào ngày <strong>{{.DateLabel}}</strong>:
      </p>
      <div class="reminder-box">
        <span class="reminder-type">{{.ReminderTypeLabel}}</span>
        <p class="reminder-title">{{.Title}}</p>
        {{if .Description}}<p class="reminder-desc">{{.Description}}</p>{{end}}
        <div class="date-info">
          <span class="date-chip">📅 {{.DateLabel}}</span>
          {{if .IsLunar}}<span class="date-chip">🌙 Âm lịch</span>{{end}}
        </div>
      </div>
      <div class="divider"></div>
      <p style="text-align:center;">
        <a href="{{.AppURL}}/profile/reminders" class="btn">Xem tất cả nhắc nhở →</a>
      </p>
    </div>
    <div class="footer">
      <p>Bạn nhận email này vì đã bật thông báo email cho nhắc nhở này.<br>
      <a href="{{.AppURL}}/profile/reminders" style="color:#c4783a;">Quản lý nhắc nhở</a> &middot; <a href="{{.AppURL}}" style="color:#c4783a;">{{.AppName}}</a></p>
    </div>
  </div>
</div>
</body>
</html>`
