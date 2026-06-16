# ============================================
# Stage 1: Build the application
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml first and download dependencies.
# This layer is cached as long as pom.xml doesn't change,
# making source-code-only changes rebuild in seconds.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source and build the fat jar.
# Skip tests in the image build — tests run in CI, not here.
COPY src ./src
RUN mvn package -DskipTests -B

# ============================================
# Stage 2: Runtime image
# ============================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create a non-root user to run the app.
# Defense in depth: if the app is exploited, the attacker
# is a non-privileged user inside an isolated container.
RUN groupadd --system app && useradd --system --gid app app

# Copy ONLY the fat jar from the builder stage.
# Maven, source, .m2 cache, JDK — all left behind in stage 1.
COPY --from=builder /build/target/*.jar app.jar

# Make sure the non-root user owns its files.
RUN chown app:app app.jar
USER app

# Document the port (does NOT publish it — that's a run-time flag).
EXPOSE 8080

# Exec form so Java is PID 1 and receives SIGTERM directly.
# Spring Boot's graceful shutdown depends on this.
ENTRYPOINT ["java", "-jar", "app.jar"]