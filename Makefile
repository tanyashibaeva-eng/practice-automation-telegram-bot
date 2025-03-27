token := $(shell cat token.secret)

up:
	@echo "Starting docker-compose with token: $(token)"
	@docker-compose up -d
