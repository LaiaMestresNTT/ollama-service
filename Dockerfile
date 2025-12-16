FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY https://github.com/LaiaMestresNTT/ollama-consumer/actions/runs/19891863160/artifacts/4749815431 app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
