FROM openjdk:23-slim
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

# FROM openjdk:23 AS buildstage
# WORKDIR /app
# COPY . .
# RUN ./gradlew clean bootJar
# COPY build/libs/*.jar app.jar
# FROM openjdk:23
# COPY --from=buildstage /app/app.jar .
# ENTRYPOINT ["java", "-jar", "app.jar"]
