FROM openjdk:11

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} cloudgatway.jar

ENTRYPOINT ["java", "-jar", "/cloudgatway.jar"]

EXPOSE 9090