# Build stage
# Build context must be ./back (set in docker-compose.yml)
FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Install parent POM to local Maven repository
COPY financial-app-parent/pom.xml financial-app-parent/pom.xml
RUN mvn -f financial-app-parent/pom.xml install -N -q

# Resolve dependencies (cached layer — only re-runs when pom.xml changes)
COPY ms-investments/pom.xml ms-investments/pom.xml
RUN mvn -f ms-investments/pom.xml dependency:resolve -q

# Build
COPY ms-investments/src ms-investments/src
RUN mvn -f ms-investments/pom.xml clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/ms-investments/target/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
