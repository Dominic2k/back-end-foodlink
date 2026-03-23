# Stage 1: Build the application
FROM amazoncorretto:17-alpine AS build

WORKDIR /application

# Copy Maven wrapper and pom.xml first (for Docker layer caching)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:resolve

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM amazoncorretto:17-alpine-jdk

LABEL description="Spring Boot base service"

WORKDIR /application

COPY --from=build /application/target/*.jar application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]