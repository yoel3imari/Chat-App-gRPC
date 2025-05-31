#!/bin/bash

# Build and push Docker images
echo "Building Docker images..."

# Build backend services
for service in admin auth user chat notification; do
    echo "Building $service-service..."
    docker build -t grpc-messenger/$service-service:latest ./backend/$service/
    # If using a registry, push the images:
    # docker push grpc-messenger/$service-service:latest
done

# Build frontend
echo "Building frontend..."
docker build -t grpc-messenger/frontend:latest ./frontend/
# docker push grpc-messenger/frontend:latest

# Apply Kubernetes manifests
echo "Deploying to Kubernetes..."

kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/mysql-init-config.yaml
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/redis.yaml

# Wait for databases to be ready
echo "Waiting for databases to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql -n grpc-messenger --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n grpc-messenger --timeout=300s

# Deploy services
kubectl apply -f k8s/admin-service.yaml
kubectl apply -f k8s/user-service.yaml
kubectl apply -f k8s/auth-service.yaml
kubectl apply -f k8s/chat-service.yaml
kubectl apply -f k8s/notification-service.yaml
kubectl apply -f k8s/frontend.yaml

# Deploy Envoy proxy
kubectl apply -f k8s/envoy.yaml

# Apply HPA
kubectl apply -f k8s/hpa.yaml

# Apply Ingress
kubectl apply -f k8s/ingress.yaml

echo "Deployment complete!"
echo "Check status with: kubectl get pods -n grpc-messenger"
echo "Access the application at: messenger.local (add to /etc/hosts if testing locally)"
echo "Access Envoy at: kubectl get svc envoy-service -n grpc-messenger"