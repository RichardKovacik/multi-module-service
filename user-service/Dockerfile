# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

#1. Copy the Maven Wrapper FIRST — it's required by every ./mvnw call below.
#    Note: .mvn is a hidden directory; it must be copied explicitly.
COPY mvnw .
COPY .mvn .mvn

# 2. Fix line endings and add execution permission (key step), windws endings are not compatible with Linux, so we need to convert them to Unix format
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# 1. First copy all pom.xml files to leverage Docker layer caching for dependencies
#    Note: The parent pom and common module pom need to be copied
COPY pom.xml .
COPY common/pom.xml common/
COPY user-service/pom.xml user-service/
COPY notify-service/pom.xml notify-service/

# 2. Download dependencies (if pom.xml unchanged, this layer will be reused)
#    Using mvnw, no need to install Maven in the image
RUN ./mvnw -B dependency:go-offline -pl user-service -am

# 3. Copy source code and build
COPY common/src common/src
COPY user-service/src user-service/src
# Skip tests because CI pipeline already has a dedicated testing phase; building the image again would be redundant
RUN ./mvnw -B clean package -pl user-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user to run the application (security best practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the built JAR from the build stage
COPY --from=builder /app/user-service/target/*.jar app.jar

# Use environment variables to pass JVM parameters for flexible adjustment of memory, etc.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]