# ============================================================================
# Stage 1: Build
# ============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# ---------------------------------------------------------------------------
# 1. Copy the Maven Wrapper FIRST — it is required by every ./mvnw call below.
# ---------------------------------------------------------------------------
COPY mvnw .
COPY .mvn .mvn

# ---------------------------------------------------------------------------
# 2. Fix CRLF line endings (Windows checkouts) and mark the wrapper executable.
# ---------------------------------------------------------------------------
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# ---------------------------------------------------------------------------
# 3. Copy all pom.xml files so the reactor can be built.
# ---------------------------------------------------------------------------
COPY pom.xml .
COPY common/pom.xml        common/
COPY user-service/pom.xml  user-service/
COPY notify-service/pom.xml notify-service/

# ---------------------------------------------------------------------------
# 4. Resolve dependencies for notify-service and its upstream chain only.
#    -pl notify-service : build only this module
#    -am                : also build modules it depends on (common)
#    user-service is intentionally NOT built.
# ---------------------------------------------------------------------------
RUN ./mvnw -B dependency:go-offline -pl notify-service -am

# ---------------------------------------------------------------------------
# 5. Copy only the source required for this module + its dependencies.
#    user-service sources are never copied → never compiled.
# ---------------------------------------------------------------------------
COPY common/src          common/src
COPY notify-service/src  notify-service/src

# ---------------------------------------------------------------------------
# 6. Build the application jar (tests already ran in CI).
# ---------------------------------------------------------------------------
RUN ./mvnw -B clean package -pl notify-service -am -DskipTests

# ============================================================================
# Stage 2: Runtime
# ============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/notify-service/target/*.jar app.jar

# notify-service is a Kafka consumer; it exposes no public HTTP port by default.
# If you later add actuator/management endpoints, uncomment and adjust:
# EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]