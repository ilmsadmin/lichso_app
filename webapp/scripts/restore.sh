#!/bin/bash
# ============================================
# Zplus Base - Database Restore Script
# ============================================
# Usage:
#   ./scripts/restore.sh postgres <backup-file.sql.gz>
#   ./scripts/restore.sh mongodb <backup-file.archive.gz>
# ============================================

set -euo pipefail

DB_TYPE="${1:-}"
BACKUP_FILE="${2:-}"

# Database credentials
POSTGRES_USER="${POSTGRES_USER:-zplus_user}"
POSTGRES_DB="${POSTGRES_DB:-zplus_db}"
MONGO_USER="${MONGO_USER:-zplus_user}"
MONGO_PASSWORD="${MONGO_PASSWORD:-zplus_secret}"
MONGO_DB="${MONGO_DB:-zplus_logs}"

if [ -z "${DB_TYPE}" ] || [ -z "${BACKUP_FILE}" ]; then
  echo "Usage: $0 <postgres|mongodb> <backup-file>"
  echo ""
  echo "Examples:"
  echo "  $0 postgres backups/postgres/daily/pg_daily_20260304_020000.sql.gz"
  echo "  $0 mongodb backups/mongodb/daily/mongo_daily_20260304_020000.archive.gz"
  exit 1
fi

if [ ! -f "${BACKUP_FILE}" ]; then
  echo "❌ Backup file not found: ${BACKUP_FILE}"
  exit 1
fi

echo "============================================"
echo "⚠️  Zplus Base - Database Restore"
echo "============================================"
echo "Type:   ${DB_TYPE}"
echo "File:   ${BACKUP_FILE}"
echo ""
echo "⚠️  WARNING: This will OVERWRITE existing data!"
read -p "Are you sure? (yes/no): " CONFIRM

if [ "${CONFIRM}" != "yes" ]; then
  echo "❌ Restore cancelled."
  exit 0
fi

case "${DB_TYPE}" in
  postgres)
    echo "🔄 Restoring PostgreSQL..."
    gunzip -c "${BACKUP_FILE}" | docker-compose exec -T postgres psql \
      -U "${POSTGRES_USER}" \
      -d "${POSTGRES_DB}" \
      --quiet
    echo "✅ PostgreSQL restore completed!"
    ;;

  mongodb)
    echo "🔄 Restoring MongoDB..."
    docker-compose exec -T mongodb mongorestore \
      --username="${MONGO_USER}" \
      --password="${MONGO_PASSWORD}" \
      --authenticationDatabase=admin \
      --db="${MONGO_DB}" \
      --archive \
      --gzip \
      --drop \
      < "${BACKUP_FILE}"
    echo "✅ MongoDB restore completed!"
    ;;

  *)
    echo "❌ Unknown database type: ${DB_TYPE}"
    echo "Supported: postgres, mongodb"
    exit 1
    ;;
esac

echo ""
echo "✅ Restore completed at $(date)"
