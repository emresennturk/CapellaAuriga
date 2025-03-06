# Java 17 tabanlı bir imaj kullan
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
# JAR dosyasını kopyala
COPY target/*.jar app.jar
# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "/app.jar"]