# Set the default target to show the available commands
.DEFAULT_GOAL := help

.PHONY: help build build-dev build-prod up up-dev up-prod down down-dev down-prod logs logs-dev restart restart-dev backend-shell frontend-shell service-shell

# Compose file variables
COMPOSE_FILE_PROD = docker-compose.yml
COMPOSE_FILE_DEV = docker-compose.dev.yml

# Display help for available commands
help:
	@echo "Makefile commands:"
	@echo ""
	@echo "Development commands (uses docker-compose.dev.yml):"
	@echo "  make dev             - Build and start dev containers with hot reload"
	@echo "  make build-dev       - Build dev containers"
	@echo "  make up-dev          - Start dev containers"
	@echo "  make down-dev        - Stop and remove dev containers"
	@echo "  make logs-dev        - Tail logs of dev containers"
	@echo "  make restart-dev     - Restart dev containers"
	@echo ""
	@echo "Production commands (uses docker-compose.yml):"
	@echo "  make prod            - Build and start production containers"
	@echo "  make build-prod      - Build production containers"
	@echo "  make up-prod         - Start production containers"
	@echo "  make down-prod       - Stop and remove production containers"
	@echo ""
	@echo "Default commands (uses docker-compose.dev.yml for development):"
	@echo "  make build           - Build dev containers"
	@echo "  make up              - Start dev containers"
	@echo "  make down            - Stop and remove dev containers"
	@echo "  make logs            - Tail logs of dev containers"
	@echo "  make restart         - Restart dev containers"
	@echo ""
	@echo "Utility commands:"
	@echo "  make no-cache-build  - Build dev containers without cache"
	@echo "  make service-shell SERVICE=<name> - Open shell in specific service"
	@echo "  make admin-shell     - Open shell in admin-service"
	@echo "  make user-shell      - Open shell in user-service"
	@echo "  make auth-shell      - Open shell in auth-service"
	@echo "  make chat-shell      - Open shell in chat-service"
	@echo "  make notification-shell - Open shell in notification-service"

# Development workflow (quick commands)
dev: build-dev up-dev

prod: build-prod up-prod

# Development commands (default)
build:
	@echo "Building dev containers..."
	docker-compose -f $(COMPOSE_FILE_DEV) build

up:
	@echo "Starting dev containers..."
	docker-compose -f $(COMPOSE_FILE_DEV) up -d

down:
	@echo "Stopping and removing dev containers..."
	docker-compose -f $(COMPOSE_FILE_DEV) down

logs:
	@echo "Tailing dev logs..."
	docker-compose -f $(COMPOSE_FILE_DEV) logs -f

restart:
	@echo "Restarting dev containers..."
	docker-compose -f $(COMPOSE_FILE_DEV) restart

# Development commands (explicit)
build-dev:
	@echo "Building dev containers..."
	docker-compose -f $(COMPOSE_FILE_DEV) build

up-dev:
	@echo "Starting dev containers..."
	docker-compose -f $(COMPOSE_FILE_DEV) up -d

down-dev:
	@echo "Stopping and removing dev containers..."
	docker-compose -f $(COMPOSE_FILE_DEV) down

logs-dev:
	@echo "Tailing dev logs..."
	docker-compose -f $(COMPOSE_FILE_DEV) logs -f

restart-dev:
	@echo "Restarting dev containers..."
	docker-compose -f $(COMPOSE_FILE_DEV) restart

# Production commands
build-prod:
	@echo "Building production containers..."
	docker-compose -f $(COMPOSE_FILE_PROD) build

up-prod:
	@echo "Starting production containers..."
	docker-compose -f $(COMPOSE_FILE_PROD) up -d

down-prod:
	@echo "Stopping and removing production containers..."
	docker-compose -f $(COMPOSE_FILE_PROD) down

# Utility commands
no-cache-build:
	@echo "Building dev containers without cache..."
	docker-compose -f $(COMPOSE_FILE_DEV) build --no-cache

# Generic service shell (usage: make service-shell SERVICE=admin-service)
service-shell:
	@if [ -z "$(SERVICE)" ]; then \
		echo "Usage: make service-shell SERVICE=<service-name>"; \
		echo "Available services: admin-service, user-service, auth-service, chat-service, notification-service"; \
	else \
		echo "Opening shell in $(SERVICE) container..."; \
		docker-compose -f $(COMPOSE_FILE_DEV) exec $(SERVICE) bash; \
	fi

# Specific service shells
admin-shell:
	@echo "Opening shell in admin-service container..."
	docker-compose -f $(COMPOSE_FILE_DEV) exec admin-service bash

user-shell:
	@echo "Opening shell in user-service container..."
	docker-compose -f $(COMPOSE_FILE_DEV) exec user-service bash

auth-shell:
	@echo "Opening shell in auth-service container..."
	docker-compose -f $(COMPOSE_FILE_DEV) exec auth-service bash

chat-shell:
	@echo "Opening shell in chat-service container..."
	docker-compose -f $(COMPOSE_FILE_DEV) exec chat-service bash

notification-shell:
	@echo "Opening shell in notification-service container..."
	docker-compose -f $(COMPOSE_FILE_DEV) exec notification-service bash

# Database and Redis shells
mysql-shell:
	@echo "Opening MySQL shell..."
	docker-compose -f $(COMPOSE_FILE_DEV) exec mysql mysql -u chatuser -p chatdb

redis-shell:
	@echo "Opening Redis CLI..."
	docker-compose -f $(COMPOSE_FILE_DEV) exec redis redis-cli

# Clean up everything
clean:
	@echo "Cleaning up containers, volumes, and networks..."
	docker-compose -f $(COMPOSE_FILE_DEV) down -v
	docker-compose -f $(COMPOSE_FILE_PROD) down -v
	docker system prune -f