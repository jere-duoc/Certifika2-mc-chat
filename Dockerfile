# ==========================================
# Etapa 1: Compilación (Build Stage)
# ==========================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copiar descriptor de dependencias
COPY pom.xml .

# Descargar dependencias para aprovechar caché
RUN mvn dependency:go-offline -B || true

# Copiar código fuente y empaquetar
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Entorno de Ejecución (Runtime Stage)
# Optimizado para AWS ECS Fargate
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Instalar curl para healthchecks de ECS
RUN apk add --no-cache curl

# Crear usuario y grupo sin privilegios de root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el artefacto JAR construido
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

# Cambiar a usuario no root
USER appuser

# Puerto expuesto por ms-chat (HTTP y WebSockets)
EXPOSE 8083

# Optimización dinámica de memoria para contenedores en ECS
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
