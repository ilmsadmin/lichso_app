#!/bin/bash
# ============================================
# Zplus Base - Health Check Script
# ============================================
# Usage: ./scripts/health-check.sh [--json]
# ============================================

JSON_OUTPUT=false
if [ "${1:-}" = "--json" ]; then
    JSON_OUTPUT=true
fi

STATUS="healthy"
RESULTS=()

# Check function
check_service() {
    local name=$1
    local cmd=$2
    local result="healthy"

    if ! eval "$cmd" > /dev/null 2>&1; then
        result="unhealthy"
        STATUS="degraded"
    fi

    RESULTS+=("{\"name\":\"$name\",\"status\":\"$result\"}")

    if [ "$JSON_OUTPUT" = false ]; then
        if [ "$result" = "healthy" ]; then
            printf "%-12s ✅ Healthy\n" "$name:"
        else
            printf "%-12s ❌ Unhealthy\n" "$name:"
        fi
    fi
}

if [ "$JSON_OUTPUT" = false ]; then
    echo "🔍 Checking services health..."
    echo ""
fi

# Check services
check_service "PostgreSQL" "docker-compose exec -T postgres pg_isready -U zplus_user -d zplus_db"
check_service "MongoDB" "docker-compose exec -T mongodb mongosh --eval 'db.adminCommand(\"ping\")' -u zplus_user -p zplus_secret --quiet"
check_service "Redis" "docker-compose exec -T redis redis-cli ping"
check_service "API" "curl -sf http://localhost:8080/api/ping"
check_service "Web" "curl -sf http://localhost:3000"
check_service "Nginx" "curl -sf http://localhost"

if [ "$JSON_OUTPUT" = true ]; then
    # Output as JSON
    SERVICES=$(IFS=,; echo "${RESULTS[*]}")
    echo "{\"status\":\"$STATUS\",\"timestamp\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",\"services\":[$SERVICES]}"
else
    echo ""
    if [ "$STATUS" = "healthy" ]; then
        echo "✅ All services are healthy!"
    else
        echo "⚠️  Some services are unhealthy!"
    fi
fi

# Exit with error code if degraded
if [ "$STATUS" != "healthy" ]; then
    exit 1
fi
