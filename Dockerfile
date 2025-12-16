FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY /home/runner/work/ollama-service/ollama-service/build app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
