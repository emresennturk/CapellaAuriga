# Java 17 ve Maven tabanlı bir imaj kullan
FROM maven:3.8.6-eclipse-temurin-21 AS build
WORKDIR /app

# Pom dosyasını kopyala ve bağımlılıkları indir
COPY pom.xml .
RUN mvn dependency:go-offline

# Tüm kaynak kodunu kopyala ve build yap
COPY src ./src
RUN mvn clean package -DskipTests

# Çalışma imajını oluştur
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]