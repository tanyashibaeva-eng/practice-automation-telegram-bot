FROM openjdk:19-jdk

WORKDIR /app

COPY . .

RUN ./gradlew --no-daemon shadowJar

ENTRYPOINT ["java", "-jar", "build/libs/practice-automation-telegram-bot.jar"]