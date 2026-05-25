#!/bin/bash
# ============================================
# Zplus Base - Database Backup Script
# ============================================
# Usage: ./scripts/backup.sh [daily|weekly|manual]
# Cron:  0 2 * * * /path/to/scripts/backup.sh daily
#        0 3 * * 0 /path/to/scripts/backup.sh weekly
# ============================================

set -euo pipefail

# Configuration
BACKUP_DIR="${BACKUP_DIR:-./backups}"
BACKUP_TYPE="${1:-manual}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS="${RETENTION_DAYS:-30}"

# Database credentials (from .env or defaults)
POSTGRES_USER="${POSTGRES_USER:-zplus_user}"
POSTGRES_DB="${POSTGRES_DB:-zplus_db}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-zplus_secret}"
MONGO_USER="${MONGO_USER:-zplus_user}"
MONGO_PASSWORD="${MONGO_PASSWORD:-zplus_secret}"
MONGO_DB="${MONGO_DB:-zplus_logs}"

# Create backup directories
PG_BACKUP_DIR="${BACKUP_DIR}/postgres/${BACKUP_TYPE}"
MONGO_BACKUP_DIR="${BACKUP_DIR}/mongodb/${BACKUP_TYPE}"
mkdir -p "${PG_BACKUP_DIR}" "${MONGO_BACKUP_DIR}"

echo "============================================"
echo "🔄 Zplus Base - Database Backup"
echo "============================================"
echo "Type:      ${BACKUP_TYPE}"
echo "Timestamp: ${TIMESTAMP}"
echo "Directory: ${BACKUP_DIR}"
echo ""

# ============================================
# PostgreSQL Backup
# ============================================
echo "📦 Backing up PostgreSQL..."
PG_BACKUP_FILE="${PG_BACKUP_DIR}/pg_${BACKUP_TYPE}_${TIMESTAMP}.sql.gz"

docker-compose exec -T postgres pg_dump \
  -U "${POSTGRES_USER}" \
  -d "${POSTGRES_DB}" \
  --no-owner \
  --no-privileges \
  --clean \
  --if-exists \
  | gzip > "${PG_BACKUP_FILE}"

PG_SIZE=$(du -h "${PG_BACKUP_FILE}" | cut -f1)
echo "  ✅ PostgreSQL backup: ${PG_BACKUP_FILE} (${PG_SIZE})"

# ============================================
# MongoDB Backup
# ============================================
echo "📦 Backing up MongoDB..."
MONGO_BACKUP_FILE="${MONGO_BACKUP_DIR}/mongo_${BACKUP_TYPE}_${TIMESTAMP}"

docker-compose exec -T mongodb mongodump \
  --username="${MONGO_USER}" \
  --password="${MONGO_PASSWORD}" \
  --authenticationDatabase=admin \
  --db="${MONGO_DB}" \
  --archive \
  --gzip \
  > "${MONGO_BACKUP_FILE}.archive.gz"

MONGO_SIZE=$(du -h "${MONGO_BACKUP_FILE}.archive.gz" | cut -f1)
echo "  ✅ MongoDB backup: ${MONGO_BACKUP_FILE}.archive.gz (${MONGO_SIZE})"

# ============================================
# Cleanup old backups
# ============================================
echo ""
echo "🧹 Cleaning up backups older than ${RETENTION_DAYS} days..."

DELETED_PG=$(find "${BACKUP_DIR}/postgres" -name "*.sql.gz" -mtime +"${RETENTION_DAYS}" -delete -print | wc -l)
DELETED_MONGO=$(find "${BACKUP_DIR}/mongodb" -name "*.archive.gz" -mtime +"${RETENTION_DAYS}" -delete -print | wc -l)

echo "  Deleted ${DELETED_PG} old PostgreSQL backups"
echo "  Deleted ${DELETED_MONGO} old MongoDB backups"

# ============================================
# Summary
# ============================================
echo ""
echo "============================================"
echo "✅ Backup completed at $(date)"
echo ""
echo "PostgreSQL: ${PG_BACKUP_FILE}"
echo "MongoDB:    ${MONGO_BACKUP_FILE}.archive.gz"
echo "============================================"
