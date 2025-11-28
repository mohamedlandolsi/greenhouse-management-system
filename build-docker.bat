@echo off
echo ============================================
echo Building all microservices for Docker...
echo ============================================

echo.
echo [1/6] Building service-discovery...
docker build -t greenhouse/service-discovery:latest -f service-discovery/Dockerfile .
if errorlevel 1 goto :error

echo.
echo [2/6] Building config-server...
docker build -t greenhouse/config-server:latest -f config-server/Dockerfile .
if errorlevel 1 goto :error

echo.
echo [3/6] Building api-gateway...
docker build -t greenhouse/api-gateway:latest -f api-gateway/Dockerfile .
if errorlevel 1 goto :error

echo.
echo [4/6] Building environnement-service...
docker build -t greenhouse/environnement-service:latest -f environnement-service/Dockerfile .
if errorlevel 1 goto :error

echo.
echo [5/6] Building controle-service...
docker build -t greenhouse/controle-service:latest -f controle-service/Dockerfile .
if errorlevel 1 goto :error

echo.
echo [6/6] Building greenhouse-dashboard...
docker build -t greenhouse/dashboard:latest -f greenhouse-dashboard/Dockerfile .
if errorlevel 1 goto :error

echo.
echo ============================================
echo All services built successfully!
echo ============================================
echo.
echo Available images:
docker images | findstr greenhouse
goto :end

:error
echo.
echo ============================================
echo BUILD FAILED!
echo ============================================
exit /b 1

:end
