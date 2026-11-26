# # Giai đoạn 1: Build file .jar bằng Maven và Java 17
# FROM maven:3.9-eclipse-temurin-17 AS build
# WORKDIR /app
# COPY pom.xml .
# COPY src ./src
# RUN mvn clean package -DskipTests

# # Giai đoạn 2: Chạy ứng dụng bằng JRE 17 
# FROM eclipse-temurin:17-jre-alpine
# WORKDIR /app
# COPY --from=build /app/target/*.jar app.jar
# EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "app.jar"]
# Giai đoạn 1: Build file .jar bằng Maven và Java 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Chuyển sang kho lưu trữ Maven của Google để tránh lỗi 429 trên Render
RUN mkdir -p /root/.m2 && printf '<settings><mirrors><mirror><id>google</id><mirrorOf>central</mirrorOf><url>https://maven-central.storage-download.googleapis.com/maven2</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng bằng JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
