#!/bin/bash
# ============================================
# Zplus Base - Initial Setup Script
# ============================================
# Usage: bash scripts/setup.sh
# ============================================

set -e

echo "🚀 Zplus Base - Initial Setup"
echo "=============================="
echo ""

# Copy environment files
echo "📋 Setting up environment files..."

if [ ! -f .env ]; then
    cp .env.example .env
    echo "  ✅ Created .env"
else
    echo "  ⏭️  .env already exists, skipping"
fi

if [ ! -f backend/.env ]; then
    cp backend/.env.example backend/.env
    echo "  ✅ Created backend/.env"
else
    echo "  ⏭️  backend/.env already exists, skipping"
fi

if [ ! -f frontend/.env.local ]; then
    cp frontend/.env.example frontend/.env.local
    echo "  ✅ Created frontend/.env.local"
else
    echo "  ⏭️  frontend/.env.local already exists, skipping"
fi

echo ""
echo "✅ Setup complete!"
echo ""
echo "Next steps:"
echo "  1. Review and update .env files with your settings"
echo "  2. Run 'make up' to start all services"
echo "  3. Run 'make migrate' to run database migrations"
echo "  4. Run 'make seed' to seed default data"
echo ""
