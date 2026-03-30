up:
	./gradlew --no-daemon shadowJar
	@docker-compose --env-file .env up --build --force-recreate --remove-orphans

prod:
	./gradlew --no-daemon shadowJar
	@docker-compose --env-file .env up app --build -d
	@sleep 5
	@docker logs practice-automation-telegram-bot-app-1