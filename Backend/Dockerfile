# ===== Stage 1: Build =====
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml first and download dependencies separately - this layer gets
# cached by Docker, so re-builds only re-download deps if pom.xml actually changes.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the actual source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== Stage 2: Run =====
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the built jar from the build stage - final image has no Maven,
# no source, no build cache, just the JRE and the runnable jar.
COPY --from=build /app/target/*.jar app.jar

# Render injects its own $PORT at runtime - actual port binding is controlled
# by application.yml's server.port: ${PORT:8081}, not by this EXPOSE line
# (EXPOSE is documentation/metadata only, doesn't affect real networking).
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]