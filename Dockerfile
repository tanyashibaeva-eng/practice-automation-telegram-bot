FROM eclipse-temurin:19-jdk

WORKDIR /app

COPY . .

#RUN if [ ! -f build/libs/practice-automation-telegram-bot.jar ]; then ./gradlew --no-daemon shadowJar; else echo "Build already exists, skipping build"; fi

CMD ["java", "-jar", "build/libs/practice-automation-telegram-bot.jar"]