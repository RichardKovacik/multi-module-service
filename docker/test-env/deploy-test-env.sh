#bash
set -e

cd ../../

pwd

echo "Building whole maven multi-module project locally..."
mvn clean install -DskipTests

echo "Building and Starting test environment..."
docker compose -f ./docker/test-env/docker-compose-env-test.yaml up --build -d

echo "Done."
