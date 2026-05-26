package main

import (
	"database/sql"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/zplus/lichso/internal/config"

	_ "github.com/jackc/pgx/v5/stdlib"
)

const migrationsTable = "schema_migrations"

type migration struct {
	Version  int
	Name     string
	UpFile   string
	DownFile string
}

func main() {
	if len(os.Args) < 2 {
		printUsage()
		os.Exit(1)
	}

	command := os.Args[1]

	// Load config
	cfg, err := config.LoadConfig()
	if err != nil {
		log.Fatalf("❌ Failed to load config: %v", err)
	}

	// Connect to PostgreSQL using stdlib driver
	dsn := cfg.Database.DSN()
	db, err := sql.Open("pgx", dsn)
	if err != nil {
		log.Fatalf("❌ Failed to connect to PostgreSQL: %v", err)
	}
	defer db.Close()

	if err := db.Ping(); err != nil {
		log.Fatalf("❌ Failed to ping PostgreSQL: %v", err)
	}

	fmt.Println("✅ Connected to PostgreSQL")

	// Ensure migrations table exists
	if err := ensureMigrationsTable(db); err != nil {
		log.Fatalf("❌ Failed to create migrations table: %v", err)
	}

	// Load migration files
	migrations, err := loadMigrations("migrations")
	if err != nil {
		log.Fatalf("❌ Failed to load migrations: %v", err)
	}

	switch command {
	case "up":
		steps := 0
		if len(os.Args) > 2 {
			steps, _ = strconv.Atoi(os.Args[2])
		}
		if err := migrateUp(db, migrations, steps); err != nil {
			log.Fatalf("❌ Migration up failed: %v", err)
		}
	case "down":
		steps := 1
		if len(os.Args) > 2 {
			steps, _ = strconv.Atoi(os.Args[2])
		}
		if err := migrateDown(db, migrations, steps); err != nil {
			log.Fatalf("❌ Migration down failed: %v", err)
		}
	case "reset":
		if err := migrateReset(db, migrations); err != nil {
			log.Fatalf("❌ Migration reset failed: %v", err)
		}
	case "status":
		if err := migrateStatus(db, migrations); err != nil {
			log.Fatalf("❌ Migration status failed: %v", err)
		}
	case "fresh":
		if err := migrateReset(db, migrations); err != nil {
			log.Fatalf("❌ Migration reset failed: %v", err)
		}
		if err := migrateUp(db, migrations, 0); err != nil {
			log.Fatalf("❌ Migration up failed: %v", err)
		}
	default:
		fmt.Printf("❌ Unknown command: %s\n", command)
		printUsage()
		os.Exit(1)
	}
}

func printUsage() {
	fmt.Print(`
Zplus Base - Migration Tool

Usage:
  go run cmd/migrate/main.go <command> [args]

Commands:
  up [N]      Run all pending migrations (or N steps)
  down [N]    Rollback last N migrations (default: 1)
  reset       Rollback all migrations
  fresh       Reset and re-run all migrations
  status      Show migration status
`)
}

// ensureMigrationsTable creates the migrations tracking table if it doesn't exist
func ensureMigrationsTable(db *sql.DB) error {
	query := fmt.Sprintf(`
		CREATE TABLE IF NOT EXISTS %s (
			version    INTEGER PRIMARY KEY,
			name       VARCHAR(255) NOT NULL,
			applied_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
		);
	`, migrationsTable)

	if _, err := db.Exec(query); err != nil {
		return err
	}

	// Safety: add missing columns if the table already existed (e.g. from standard golang-migrate)
	_, _ = db.Exec(fmt.Sprintf("ALTER TABLE %s ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL DEFAULT ''", migrationsTable))
	_, _ = db.Exec(fmt.Sprintf("ALTER TABLE %s ADD COLUMN IF NOT EXISTS applied_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()", migrationsTable))
	return nil
}

// loadMigrations discovers migration files from the migrations directory
func loadMigrations(dir string) ([]migration, error) {
	files, err := filepath.Glob(filepath.Join(dir, "*.sql"))
	if err != nil {
		return nil, fmt.Errorf("failed to glob migration files: %w", err)
	}

	migrationMap := make(map[int]*migration)

	for _, f := range files {
		base := filepath.Base(f)
		parts := strings.SplitN(base, "_", 2)
		if len(parts) < 2 {
			continue
		}

		version, err := strconv.Atoi(parts[0])
		if err != nil {
			continue
		}

		m, ok := migrationMap[version]
		if !ok {
			m = &migration{Version: version}
			migrationMap[version] = m
		}

		// Extract migration name (remove version prefix and .up.sql/.down.sql suffix)
		name := parts[1]
		name = strings.TrimSuffix(name, ".up.sql")
		name = strings.TrimSuffix(name, ".down.sql")
		m.Name = name

		if strings.HasSuffix(base, ".up.sql") {
			m.UpFile = f
		} else if strings.HasSuffix(base, ".down.sql") {
			m.DownFile = f
		}
	}

	// Convert map to sorted slice
	var migrations []migration
	for _, m := range migrationMap {
		migrations = append(migrations, *m)
	}
	sort.Slice(migrations, func(i, j int) bool {
		return migrations[i].Version < migrations[j].Version
	})

	return migrations, nil
}

// getAppliedVersions returns the set of applied migration versions
func getAppliedVersions(db *sql.DB) (map[int]bool, error) {
	rows, err := db.Query(fmt.Sprintf("SELECT version FROM %s ORDER BY version", migrationsTable))
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	applied := make(map[int]bool)
	for rows.Next() {
		var version int
		if err := rows.Scan(&version); err != nil {
			return nil, err
		}
		applied[version] = true
	}
	return applied, rows.Err()
}

// migrateUp runs pending migrations
func migrateUp(db *sql.DB, migrations []migration, steps int) error {
	applied, err := getAppliedVersions(db)
	if err != nil {
		return fmt.Errorf("failed to get applied versions: %w", err)
	}

	count := 0
	for _, m := range migrations {
		if applied[m.Version] {
			continue
		}

		if steps > 0 && count >= steps {
			break
		}

		if m.UpFile == "" {
			return fmt.Errorf("migration %d (%s): up file not found", m.Version, m.Name)
		}

		content, err := os.ReadFile(m.UpFile)
		if err != nil {
			return fmt.Errorf("migration %d: failed to read file: %w", m.Version, err)
		}

		fmt.Printf("⬆️  Running migration %06d_%s...\n", m.Version, m.Name)

		tx, err := db.Begin()
		if err != nil {
			return fmt.Errorf("migration %d: failed to begin transaction: %w", m.Version, err)
		}

		if _, err := tx.Exec(string(content)); err != nil {
			tx.Rollback()
			return fmt.Errorf("migration %d: failed to execute: %w", m.Version, err)
		}

		if _, err := tx.Exec(
			fmt.Sprintf("INSERT INTO %s (version, name) VALUES ($1, $2)", migrationsTable),
			m.Version, m.Name,
		); err != nil {
			tx.Rollback()
			return fmt.Errorf("migration %d: failed to record: %w", m.Version, err)
		}

		if err := tx.Commit(); err != nil {
			return fmt.Errorf("migration %d: failed to commit: %w", m.Version, err)
		}

		count++
		fmt.Printf("   ✅ Applied migration %06d_%s\n", m.Version, m.Name)
	}

	if count == 0 {
		fmt.Println("✅ No pending migrations")
	} else {
		fmt.Printf("✅ Applied %d migration(s)\n", count)
	}

	return nil
}

// migrateDown rolls back migrations
func migrateDown(db *sql.DB, migrations []migration, steps int) error {
	applied, err := getAppliedVersions(db)
	if err != nil {
		return fmt.Errorf("failed to get applied versions: %w", err)
	}

	// Reverse order for rollback
	reversed := make([]migration, len(migrations))
	copy(reversed, migrations)
	sort.Slice(reversed, func(i, j int) bool {
		return reversed[i].Version > reversed[j].Version
	})

	count := 0
	for _, m := range reversed {
		if !applied[m.Version] {
			continue
		}

		if steps > 0 && count >= steps {
			break
		}

		if m.DownFile == "" {
			return fmt.Errorf("migration %d (%s): down file not found", m.Version, m.Name)
		}

		content, err := os.ReadFile(m.DownFile)
		if err != nil {
			return fmt.Errorf("migration %d: failed to read file: %w", m.Version, err)
		}

		fmt.Printf("⬇️  Rolling back migration %06d_%s...\n", m.Version, m.Name)

		tx, err := db.Begin()
		if err != nil {
			return fmt.Errorf("migration %d: failed to begin transaction: %w", m.Version, err)
		}

		if _, err := tx.Exec(string(content)); err != nil {
			tx.Rollback()
			return fmt.Errorf("migration %d: failed to execute: %w", m.Version, err)
		}

		if _, err := tx.Exec(
			fmt.Sprintf("DELETE FROM %s WHERE version = $1", migrationsTable),
			m.Version,
		); err != nil {
			tx.Rollback()
			return fmt.Errorf("migration %d: failed to remove record: %w", m.Version, err)
		}

		if err := tx.Commit(); err != nil {
			return fmt.Errorf("migration %d: failed to commit: %w", m.Version, err)
		}

		count++
		fmt.Printf("   ✅ Rolled back migration %06d_%s\n", m.Version, m.Name)
	}

	if count == 0 {
		fmt.Println("✅ No migrations to rollback")
	} else {
		fmt.Printf("✅ Rolled back %d migration(s)\n", count)
	}

	return nil
}

// migrateReset rolls back all migrations
func migrateReset(db *sql.DB, migrations []migration) error {
	fmt.Println("🔄 Resetting all migrations...")
	return migrateDown(db, migrations, 0)
}

// migrateStatus shows migration status
func migrateStatus(db *sql.DB, migrations []migration) error {
	applied, err := getAppliedVersions(db)
	if err != nil {
		return fmt.Errorf("failed to get applied versions: %w", err)
	}

	// Get applied_at timestamps
	rows, err := db.Query(fmt.Sprintf("SELECT version, applied_at FROM %s ORDER BY version", migrationsTable))
	if err != nil {
		return err
	}
	defer rows.Close()

	appliedAt := make(map[int]time.Time)
	for rows.Next() {
		var version int
		var at time.Time
		if err := rows.Scan(&version, &at); err != nil {
			return err
		}
		appliedAt[version] = at
	}

	fmt.Println("\n📋 Migration Status:")
	fmt.Println("─────────────────────────────────────────────────────────────")
	fmt.Printf("%-8s %-35s %-10s %s\n", "VERSION", "NAME", "STATUS", "APPLIED AT")
	fmt.Println("─────────────────────────────────────────────────────────────")

	for _, m := range migrations {
		status := "⬜ Pending"
		appliedTime := ""
		if applied[m.Version] {
			status = "✅ Applied"
			if t, ok := appliedAt[m.Version]; ok {
				appliedTime = t.Format("2006-01-02 15:04:05")
			}
		}
		fmt.Printf("%-8d %-35s %-10s %s\n", m.Version, m.Name, status, appliedTime)
	}

	fmt.Println("─────────────────────────────────────────────────────────────")
	fmt.Printf("Total: %d migrations, %d applied, %d pending\n",
		len(migrations), len(applied), len(migrations)-len(applied))
	fmt.Println()

	return nil
}
