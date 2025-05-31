# gRPC-Messenger

A full-stack, microservices-based messenger application leveraging gRPC for efficient communication between backend services and a modern Vue 3 + TypeScript frontend.

## Features
- Microservices architecture: Auth, User, Chat, Notification, Admin
- gRPC-based service communication
- Vue 3 frontend with TypeScript, TailwindCSS, and Vite
- Dockerized for easy local development and deployment
- Kubernetes manifests for production-ready orchestration

## Project Structure
```
backend/         # Java Spring Boot microservices (auth, user, chat, notification, admin)
frontend/        # Vue 3 + TypeScript client app
proto/           # Shared protobuf definitions
k8s/             # Kubernetes manifests
mysql/           # MySQL initialization scripts
envoy/           # Envoy proxy configuration
docker-compose.yml, docker-compose.dev.yml # Local dev orchestration
```

## Development Requirements
- Java 17+
- Node.js 18+ and pnpm
- Docker & Docker Compose
- buf (for protobuf management)

### Install Frontend Tools
```sh
cd frontend
pnpm install
```

### Install Protobuf Tools
```sh
pnpm add -g @connectrpc/protoc-gen-connect-es @bufbuild/protoc-gen-es @connectrpc/connect
```

### Install Backend Tools
- Use Maven Wrapper (`./mvnw`) for Java services

## Running Locally
### Start All Services
```sh
docker-compose up --build
```

### Frontend Only (for development)
```sh
cd frontend
pnpm dev
```

### Backend Only (for development)
Run each service from its directory:
```sh
cd backend/auth && ./mvnw spring-boot:run
cd backend/user && ./mvnw spring-boot:run
# ...repeat for other services
```

## Protobuf Management
- Edit proto files in `/proto`.
- Use `update-protos.sh` to regenerate code for frontend/backend.

## Kubernetes Deployment
- See `k8s/` for manifests.
- Example:
```sh
kubectl apply -f k8s/
```

## Environment Variables
- Copy `.env.example` to `.env` and fill in required values.

## License
MIT