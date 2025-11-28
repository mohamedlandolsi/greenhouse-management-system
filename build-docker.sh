#!/bin/bash
set -e

echo "============================================"
echo "Building all microservices for Docker..."
echo "============================================"

# Function to build a service
build_service() {
    local name=$1
    local dockerfile=$2
    local counter=$3
    local total=$4
    
    echo ""
    echo "[$counter/$total] Building $name..."
    docker build -t greenhouse/$name:latest -f $dockerfile .
    echo "✓ $name built successfully"
}

# Build all services from root directory
build_service "service-discovery" "service-discovery/Dockerfile" 1 6
build_service "config-server" "config-server/Dockerfile" 2 6
build_service "api-gateway" "api-gateway/Dockerfile" 3 6
build_service "environnement-service" "environnement-service/Dockerfile" 4 6
build_service "controle-service" "controle-service/Dockerfile" 5 6
build_service "dashboard" "greenhouse-dashboard/Dockerfile" 6 6

echo ""
echo "============================================"
echo "All services built successfully!"
echo "============================================"
echo ""
echo "Available images:"
docker images | grep greenhouse
