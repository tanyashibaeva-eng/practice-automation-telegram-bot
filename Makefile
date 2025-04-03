up:
	./gradlew --no-daemon shadowJar
	@docker-compose --env-file .env up --build --force-recreate --remove-orphans

