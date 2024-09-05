FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
RUN mkdir /app/
COPY /target/discordbot-0.0.1-SNAPSHOT.jar /app/
RUN cd /app/ && jar -xvf /app/discordbot-0.0.1-SNAPSHOT.jar && rm /app/discordbot-0.0.1-SNAPSHOT.jar
EXPOSE 8080
CMD java -cp /app/org.springframework.boot.loader.launch.JarLauncher