FROM eclipse-temurin:21-jre
ENV TZ=Asia/Tashkent
WORKDIR /app
COPY app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]