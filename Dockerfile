# ---- Stage 1: Build ----
FROM gradle:8.10-jdk21 AS builder
WORKDIR /home/gradle/project

# Copiar archivos de configuración primero (para cachear dependencias)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Descargar dependencias (esto se cachea)
RUN ./gradlew build -x test --no-daemon || true

# Copiar el resto del código
COPY . .

# Construir el jar (sin correr tests para acelerar)
RUN ./gradlew clean bootJar --no-daemon -x test

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el jar (sin hardcodear nombre)
COPY --from=builder /home/gradle/project/applications/app-service/build/libs/*.jar app.jar

# Puerto definido para reports
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
