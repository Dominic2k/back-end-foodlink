FROM amazoncorretto:21-alpine-jdk

LABEL description="Spring Boot base service"

WORKDIR /application

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]
