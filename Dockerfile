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
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Tải và lưu cache thư viện trước
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Sau đó mới copy code và build
COPY src ./src
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng bằng JRE 17 
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
