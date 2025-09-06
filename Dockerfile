FROM ubuntu:latest
# Etapa 1: Construcción con Gradle
FROM gradle:8.10-jdk21 AS builder

# Directorio de trabajo
WORKDIR /home/gradle/project

# Copiar los archivos de configuración primero para cachear dependencias
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Descargar dependencias
RUN ./gradlew dependencies --no-daemon || return 0

# Copiar el resto del código
COPY . .

# Construir el jar
RUN ./gradlew clean bootJar --no-daemon

# Etapa 2: Imagen final
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el .jar generado
COPY --from=builder /home/gradle/project/applications/app-service/build/libs/CrediYa_Solicitudes.jar app.jar

# Puerto que usa tu microservicio (configurable en application.yml)
EXPOSE 8081

# Comando de inicio
ENTRYPOINT ["java","-jar","app.jar"]
