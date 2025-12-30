#!/bin/bash

# =============================================================================
# Greenhouse Management System - Kubernetes Deployment Script
# =============================================================================
# Usage: ./deploy.sh [dev|prod] [--build] [--delete]
#
# Examples:
#   ./deploy.sh dev           # Deploy to development
#   ./deploy.sh prod          # Deploy to production
#   ./deploy.sh dev --build   # Build images and deploy
#   ./deploy.sh dev --delete  # Delete deployment
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="${1:-dev}"
ACTION="${2:-apply}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# =============================================================================
# Helper Functions
# =============================================================================

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# =============================================================================
# Prerequisite Checks
# =============================================================================

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check kubectl
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed. Please install it first."
        exit 1
    fi
    
    # Check kustomize (or kubectl version supports kustomize)
    if ! kubectl version --client &> /dev/null; then
        log_error "kubectl is not properly configured."
        exit 1
    fi
    
    # Check cluster connection
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
        exit 1
    fi
    
    log_success "All prerequisites met."
}

# =============================================================================
# Build Docker Images
# =============================================================================

build_images() {
    log_info "Building Docker images..."
    
    cd "$PROJECT_ROOT"
    
    # Build backend services
    log_info "Building backend services..."
    mvn clean package -DskipTests
    
    # Build Docker images
    log_info "Building Docker images..."
    docker compose build
    
    log_success "Docker images built successfully."
}

# =============================================================================
# Deploy to Kubernetes
# =============================================================================

deploy() {
    local env=$1
    local overlay_path="$SCRIPT_DIR/overlays/$env"
    
    if [[ ! -d "$overlay_path" ]]; then
        log_error "Overlay '$env' not found at $overlay_path"
        exit 1
    fi
    
    log_info "Deploying to $env environment..."
    
    # Validate manifests
    log_info "Validating manifests..."
    kubectl apply --dry-run=client -k "$overlay_path"
    
    # Apply manifests
    log_info "Applying manifests..."
    kubectl apply -k "$overlay_path"
    
    # Wait for deployments
    log_info "Waiting for deployments to be ready..."
    local namespace="greenhouse-$env"
    
    # Wait for infrastructure first
    log_info "Waiting for infrastructure services..."
    kubectl wait --for=condition=ready pod -l app=postgres-env -n "$namespace" --timeout=120s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=postgres-ctrl -n "$namespace" --timeout=120s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=zookeeper -n "$namespace" --timeout=120s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=kafka -n "$namespace" --timeout=180s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=redis -n "$namespace" --timeout=60s 2>/dev/null || true
    
    # Wait for application services
    log_info "Waiting for application services..."
    kubectl wait --for=condition=ready pod -l app=service-discovery -n "$namespace" --timeout=180s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=config-server -n "$namespace" --timeout=180s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=api-gateway -n "$namespace" --timeout=180s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=environnement-service -n "$namespace" --timeout=180s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=controle-service -n "$namespace" --timeout=180s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=frontend -n "$namespace" --timeout=120s 2>/dev/null || true
    
    log_success "Deployment to $env completed!"
}

# =============================================================================
# Delete Deployment
# =============================================================================

delete_deployment() {
    local env=$1
    local overlay_path="$SCRIPT_DIR/overlays/$env"
    
    log_warning "Deleting $env deployment..."
    kubectl delete -k "$overlay_path" --ignore-not-found
    
    log_success "Deployment deleted."
}

# =============================================================================
# Show Status
# =============================================================================

show_status() {
    local env=$1
    local namespace="greenhouse-$env"
    
    echo ""
    log_info "=== Deployment Status ==="
    echo ""
    
    log_info "Pods:"
    kubectl get pods -n "$namespace" -o wide 2>/dev/null || echo "No pods found"
    
    echo ""
    log_info "Services:"
    kubectl get services -n "$namespace" 2>/dev/null || echo "No services found"
    
    echo ""
    log_info "Ingress:"
    kubectl get ingress -n "$namespace" 2>/dev/null || echo "No ingress found"
    
    echo ""
    log_info "=== Access URLs ==="
    if [[ "$env" == "dev" ]]; then
        echo "  Frontend:  http://greenhouse.local"
        echo "  API:       http://greenhouse.local/api"
        echo "  Eureka:    http://greenhouse.local/eureka"
    else
        echo "  Frontend:  https://greenhouse.example.com"
        echo "  API:       https://greenhouse.example.com/api"
    fi
    echo ""
    log_info "Add '127.0.0.1 greenhouse.local' to /etc/hosts for local access"
}

# =============================================================================
# Main
# =============================================================================

main() {
    echo ""
    echo "=============================================="
    echo "  Greenhouse Kubernetes Deployment"
    echo "=============================================="
    echo ""
    
    # Validate environment
    if [[ "$ENVIRONMENT" != "dev" && "$ENVIRONMENT" != "prod" ]]; then
        log_error "Invalid environment: $ENVIRONMENT"
        echo "Usage: $0 [dev|prod] [--build|--delete]"
        exit 1
    fi
    
    check_prerequisites
    
    # Handle actions
    case "$ACTION" in
        --build)
            build_images
            deploy "$ENVIRONMENT"
            ;;
        --delete)
            delete_deployment "$ENVIRONMENT"
            ;;
        *)
            deploy "$ENVIRONMENT"
            ;;
    esac
    
    show_status "$ENVIRONMENT"
}

main "$@"
