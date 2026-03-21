FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

# baixar jar
ADD https://repo1.maven.org/maven2/org/zeromq/jeromq/0.5.3/jeromq-0.5.3.jar .

RUN javac -cp jeromq-0.5.3.jar *.java

CMD ["java", "-cp", ".:jeromq-0.5.3.jar", "Broker"]