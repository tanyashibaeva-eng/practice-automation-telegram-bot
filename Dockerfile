FROM openjdk:19-jdk

WORKDIR /app

# COPY src gradlew gradle build.gradle settings.gradle .
COPY . .

RUN ./gradlew clean build

ARG JAR_FILE=build/libs/practice-automation-telegram-bot.jar

ENTRYPOINT ["java", "-jar", "build/libs/practice-automation-telegram-bot.jar"]