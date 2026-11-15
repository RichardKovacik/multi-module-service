#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------------------------------------------------
# 0. Parse command-line arguments
# ---------------------------------------------------------------------------
# Usage: ./start-env.sh [local|registry]
#   local    - Builds JARs via Maven and builds Docker images locally (default)
#   registry - Pulls pre-built Docker images from the registry and skips local build
MODE="${1:-local}"

if [[ "${MODE}" != "local" && "${MODE}" != "registry" ]]; then
  echo "❌ Error: Invalid mode '${MODE}'."
  echo "Usage: $0 [local|registry]"
  exit 1
fi

echo "▶ Running test environment in: ${MODE^^} mode"

# ---------------------------------------------------------------------------
# 1. Resolve paths
# ---------------------------------------------------------------------------
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/../.." && pwd)"

# We cd into REPO_ROOT so the Docker build context and Maven wrapper work cleanly
cd "${REPO_ROOT}"

echo "▶ Repo root resolved to: ${REPO_ROOT}"
echo "▶ Test env directory:    ${SCRIPT_DIR}"

# ---------------------------------------------------------------------------
# 2. Enforce manual .env creation (in the test-env directory!)
# ---------------------------------------------------------------------------
if [[ ! -f "${SCRIPT_DIR}/.env" ]]; then
  echo ""
  echo "❌ Error: .env file not found in docker/test-env/"
  echo ""
  echo "This test environment requires its own .env file with custom"
  echo "configuration (database passwords, API keys, etc.)."
  echo ""
  echo "Please create it manually:"
  echo "  1. cd docker/test-env"
  echo "  2. cp .env.example .env"
  echo "  3. Fill in required values in .env"
  echo "  4. Run this script again."
  echo ""
  exit 1
fi

# ---------------------------------------------------------------------------
# 3. Conditional Setup based on Mode (Local vs. Registry)
# ---------------------------------------------------------------------------
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose-${MODE}.yaml"
COMPOSE_ARGS=(-f "${COMPOSE_FILE}" --env-file "${SCRIPT_DIR}/.env")

if [[ "${MODE}" == "local" ]]; then
  echo "▶ [Local Mode] Preparing Maven build..."
  [[ -f mvnw ]] || { echo "❌ mvnw not found at repo root." >&2; exit 1; }

  echo "  Building JARs with Maven (skipping tests)..."
  ./mvnw clean package -DskipTests

  # Build only the service images from the local Compose configuration.
  docker compose "${COMPOSE_ARGS[@]}" build user-service notify-service
else
  echo "▶ [Registry Mode] Skipping local builds; Compose will pull service images using pull_policy: always..."
fi

# ---------------------------------------------------------------------------
# 4. Start the environment
# ---------------------------------------------------------------------------
echo "▶ Starting the test environment..."

# Start without rebuilding; Compose recreates only services with changed image/configuration.
docker compose "${COMPOSE_ARGS[@]}" up --no-build -d --wait --wait-timeout 300

# ---------------------------------------------------------------------------
# 5. Success message
# docker compose -f docker-compose-registry.yaml down -v
# ---------------------------------------------------------------------------
echo ""
echo "✅ Test environment is up and running (${MODE} mode)!"
echo ""
echo "🌐 Swagger UI:    http://localhost:8081/swagger-ui/index.html"
echo "📊 Kafka UI:      http://localhost:${KAFKA_UI_PORT:-8090}"
echo "🐘 PostgreSQL:    localhost:5433"
echo ""
echo "Useful commands:"
echo "  docker compose -f ${COMPOSE_FILE} --env-file ${SCRIPT_DIR}/.env ps"
echo "  docker compose -f ${COMPOSE_FILE} --env-file ${SCRIPT_DIR}/.env logs -f user-service"
echo "  docker compose -f ${COMPOSE_FILE} --env-file ${SCRIPT_DIR}/.env down"