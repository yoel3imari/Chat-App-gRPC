#!/bin/bash

# Start Minikube
minikube start --memory=4096 --cpus=4

# Enable ingress addon
minikube addons enable ingress

# Set Docker environment to use Minikube's Docker daemon
eval $(minikube docker-env)

# Now run the deploy.sh script
./deploy.sh

# Get the Minikube IP
MINIKUBE_IP=$(minikube ip)
echo "Add the following line to your /etc/hosts file:"
echo "$MINIKUBE_IP messenger.local"