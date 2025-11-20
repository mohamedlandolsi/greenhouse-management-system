@echo off
echo Building all microservices for Docker...

echo Building service-discovery...
cd service-discovery
docker build -t greenhouse/service-discovery:latest .
cd ..

echo Building config-server...
cd config-server
docker build -t greenhouse/config-server:latest .
cd ..

echo Building api-gateway...
cd api-gateway
docker build -t greenhouse/api-gateway:latest .
cd ..

echo Building environnement-service...
cd environnement-service
docker build -t greenhouse/environnement-service:latest .
cd ..

echo Building controle-service...
cd controle-service
docker build -t greenhouse/controle-service:latest .
cd ..

echo All services built successfully!
