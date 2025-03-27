FROM openjdk:19-jdk

WORKDIR /app

COPY . .

RUN ./gradlew clean build

ARG JAR_FILE=build/libs/practice-automation-telegram-bot.jar

ENTRYPOINT ["java", "-jar", "build/libs/practice-automation-telegram-bot.jar"]