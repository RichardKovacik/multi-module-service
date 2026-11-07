#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------------------------------------------------
# 1. Resolve paths
# ---------------------------------------------------------------------------
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/../.." && pwd)"

# We cd into REPO_ROOT just so the Docker build context works cleanly
cd "${REPO_ROOT}"

echo "▶ Repo root resolved to: ${REPO_ROOT}"
echo "▶ Test env directory:    ${SCRIPT_DIR}"

# ---------------------------------------------------------------------------
# 2. Enforce manual .env creation (in the test-env directory!)
# ---------------------------------------------------------------------------
# Notice we check for .env inside SCRIPT_DIR, not REPO_ROOT
if [[ ! -f "${SCRIPT_DIR}/.env" ]]; then
  echo ""
  echo "❌ Error: .env file not found in docker/test-env/"
  echo ""
  echo "This test environment requires its own .env file with custom"
  echo "configuration (database passwords, API keys, etc.)."
  echo ""
  echo "Please create it manually:"
  echo ""
  echo "  1. Navigate to the test-env directory:"
  echo "     cd docker/test-env"
  echo ""
  echo "  2. Copy the example file:"
  echo "     cp .env.example .env"
  echo ""
  echo "  3. Open .env in your editor and fill in the required values"
  echo "     (especially API keys like RESEND_API_KEY)."
  echo ""
  echo "  4. Run this script again."
  echo ""
  exit 1
fi

# ---------------------------------------------------------------------------
# 3. Ensure the Maven wrapper is executable on the host
# ---------------------------------------------------------------------------
[[ -f mvnw ]] || { echo "❌ mvnw not found at repo root." >&2; exit 1; }
chmod +x mvnw

# ---------------------------------------------------------------------------
# 4. Build and start the environment
# ---------------------------------------------------------------------------
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose-env-test.yaml"

echo "▶ Building and starting the test environment..."
echo "  (This may take a few minutes on the first run while Maven downloads dependencies)"

# We MUST use --env-file to explicitly point to the test-env .env file.
# Otherwise, if the user runs this from the project root, Compose might
# accidentally pick up the wrong .env file.
docker compose -f "${COMPOSE_FILE}" --env-file "${SCRIPT_DIR}/.env" up --build -d --wait --wait-timeout 300

# ---------------------------------------------------------------------------
# 5. Success message
# ---------------------------------------------------------------------------
echo ""
echo "✅ Test environment is up and running!"
echo ""
echo "🌐 Swagger UI:    http://localhost:8081/swagger-ui/index.html"
echo "📊 Kafka UI:      http://localhost:${KAFKA_UI_PORT:-8090}"
echo "🐘 PostgreSQL:    localhost:5433"
echo ""
echo "Useful commands:"
echo "  docker compose -f ${COMPOSE_FILE} --env-file ${SCRIPT_DIR}/.env ps"
echo "  docker compose -f ${COMPOSE_FILE} --env-file ${SCRIPT_DIR}/.env logs -f user-service"
echo "  docker compose -f ${COMPOSE_FILE} --env-file ${SCRIPT_DIR}/.env down"