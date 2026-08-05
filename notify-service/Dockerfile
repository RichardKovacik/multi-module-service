FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY notify-service/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]