#!/bin/bash
# ============================================
# Lịch Số - Deploy Script
# Build, push to registry, deploy on server
# ============================================

set -e

# Configuration
SERVER="42.96.17.167"
SSH_PORT="8430"
SSH_USER="root"
REGISTRY="localhost:5000"
REMOTE_DIR="/opt/zplus-lichso"
IMAGE_TAG="${1:-latest}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${BLUE}[$(date '+%H:%M:%S')]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[✗]${NC} $1"; exit 1; }

# Get project root (parent of deploy/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

log "🚀 Lịch Số - Deploy to Production"
log "Server: ${SERVER} | Registry: ${REGISTRY} | Tag: ${IMAGE_TAG}"
echo ""

# Load production env vars (for build args)
if [ -f "${SCRIPT_DIR}/.env" ]; then
  set -o allexport
  source "${SCRIPT_DIR}/.env"
  set +o allexport
fi

# ============================================
# Step 1: Build Docker images locally
# ============================================
log "📦 Building backend image (linux/amd64)..."
docker build --platform linux/amd64 \
  -t lichso-backend:${IMAGE_TAG} \
  --target production \
  -f "${PROJECT_ROOT}/backend/Dockerfile" \
  "${PROJECT_ROOT}/backend"
success "Backend image built"

log "📦 Building frontend image (linux/amd64)..."
docker build --platform linux/amd64 \
  -t lichso-frontend:${IMAGE_TAG} \
  --target production \
  --build-arg NEXT_PUBLIC_API_URL=https://lichso.vn/api \
  --build-arg NEXT_PUBLIC_APP_URL=https://lichso.vn \
  --build-arg NEXT_PUBLIC_APP_NAME="Lịch Số" \
  --build-arg NEXT_PUBLIC_ENABLE_SOCIAL_LOGIN=true \
  --build-arg NEXT_PUBLIC_GOOGLE_CLIENT_ID="${GOOGLE_CLIENT_ID:-}" \
  --build-arg NEXT_PUBLIC_FACEBOOK_APP_ID="${FACEBOOK_APP_ID:-}" \
  -f "${PROJECT_ROOT}/frontend/Dockerfile" \
  "${PROJECT_ROOT}/frontend"
success "Frontend image built"

# ============================================
# Step 2: Tag for registry (on server)
# ============================================
log "🏷️  Tagging images for server registry..."
docker tag lichso-backend:${IMAGE_TAG} ${REGISTRY}/lichso-backend:${IMAGE_TAG}
docker tag lichso-frontend:${IMAGE_TAG} ${REGISTRY}/lichso-frontend:${IMAGE_TAG}
success "Images tagged"

# ============================================
# Step 3: Transfer images to server via docker save | ssh docker load
# ============================================
log "⬆️  Transferring backend image to server..."
docker save ${REGISTRY}/lichso-backend:${IMAGE_TAG} | gzip | \
  ssh -o ServerAliveInterval=30 -o ServerAliveCountMax=10 \
  -p ${SSH_PORT} ${SSH_USER}@${SERVER} 'gunzip | docker load'
success "Backend image transferred"

log "⬆️  Transferring frontend image to server..."
docker save ${REGISTRY}/lichso-frontend:${IMAGE_TAG} | gzip | \
  ssh -o ServerAliveInterval=30 -o ServerAliveCountMax=10 \
  -p ${SSH_PORT} ${SSH_USER}@${SERVER} 'gunzip | docker load'
success "Frontend image transferred"

# ============================================
# Step 4: Upload compose & env files
# ============================================
log "📄 Uploading deployment files..."
scp -P ${SSH_PORT} "${SCRIPT_DIR}/docker-compose.yml" ${SSH_USER}@${SERVER}:${REMOTE_DIR}/
scp -P ${SSH_PORT} "${SCRIPT_DIR}/.env" ${SSH_USER}@${SERVER}:${REMOTE_DIR}/
success "Deployment files uploaded"

# ============================================
# Step 5: Deploy on server
# ============================================
log "🚀 Deploying on server..."
ssh -p ${SSH_PORT} ${SSH_USER}@${SERVER} << EOF
  cd ${REMOTE_DIR}
  
  echo "Restarting services with new images..."
  docker compose up -d --force-recreate api web
  
  echo "Running database migrations..."
  docker compose exec -T api ./migrate up
  
  echo ""
  echo "Container status:"
  docker compose ps
EOF
success "Deployment complete!"

echo ""
log "🌐 Site: https://lichso.vn"
log "📊 Check logs: ssh -p ${SSH_PORT} ${SSH_USER}@${SERVER} 'cd ${REMOTE_DIR} && docker compose logs -f'"
