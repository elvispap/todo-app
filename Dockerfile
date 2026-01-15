FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

EXPOSE 8080
COPY --from=build /app/build/libs/*.jar app.jar

# Set profile according to the environment
ENTRYPOINT ["java", "-Dspring.profiles.active=development", "-jar", "app.jar"]