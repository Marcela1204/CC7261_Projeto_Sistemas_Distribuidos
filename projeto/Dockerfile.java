FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

COPY java /app

RUN mvn package

CMD ["java","-cp","target/server-1.0.jar","chat.Server"]