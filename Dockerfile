FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/discordbot-0.0.1-SNAPSHOT.jar /app/
RUN cd /app/ && jar -xvf /app/discordbot-0.0.1-SNAPSHOT.jar && rm /app/discordbot-0.0.1-SNAPSHOT.jar
EXPOSE 8080
CMD java -Dspring.profiles.active=prod -cp /app/ org.springframework.boot.loader.launch.JarLauncher