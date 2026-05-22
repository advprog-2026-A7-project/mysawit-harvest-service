#!/bin/bash

set -e

IMAGE_NAME="mysawit-harvest-service"
CONTAINER_NAME="mysawit-harvest-service"
MAX_HISTORY=4
HEALTH_URL="http://localhost:8083/actuator/health"
HEALTH_RETRIES=12
HEALTH_INTERVAL=5

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC}  $1"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

VERSION="v$(date +%Y%m%d%H%M%S)"
log_info "Starting deployment of version: ${VERSION}"

log_info "Building Docker image: ${IMAGE_NAME}:${VERSION}..."
docker build -t "${IMAGE_NAME}:${VERSION}" .
log_success "Docker image built: ${IMAGE_NAME}:${VERSION}"

PREVIOUS_VERSION=$(docker inspect --format='{{.Config.Image}}' "${CONTAINER_NAME}" 2>/dev/null || echo "none")
log_info "Previous running version: ${PREVIOUS_VERSION}"

docker tag "${IMAGE_NAME}:${VERSION}" "${IMAGE_NAME}:current"
log_info "Tagged ${IMAGE_NAME}:${VERSION} as 'current'."

log_info "Stopping old container (if running)..."
docker compose -f docker-compose.app.yml down 2>/dev/null || true

log_info "Starting new container with version ${VERSION}..."
docker compose -f docker-compose.app.yml up -d
log_success "Container started."

log_info "Waiting for health check at ${HEALTH_URL}..."
HEALTHY=false
for i in $(seq 1 $HEALTH_RETRIES); do
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${HEALTH_URL}" 2>/dev/null || echo "000")
    if [ "${HTTP_STATUS}" = "200" ]; then
        HEALTHY=true
        break
    fi
    log_warn "Health check attempt ${i}/${HEALTH_RETRIES} — status: ${HTTP_STATUS}. Retrying in ${HEALTH_INTERVAL}s..."
    sleep $HEALTH_INTERVAL
done

if [ "${HEALTHY}" = false ]; then
    log_error "Health check FAILED after ${HEALTH_RETRIES} attempts. Rolling back..."

    docker compose -f docker-compose.app.yml down 2>/dev/null || true

    if [ "${PREVIOUS_VERSION}" != "none" ]; then
        docker tag "${PREVIOUS_VERSION}" "${IMAGE_NAME}:current"
        docker compose -f docker-compose.app.yml up -d
        log_warn "Rollback complete. Now running: ${PREVIOUS_VERSION}"
    else
        log_error "No previous version found to roll back to. Service is DOWN!"
        exit 1
    fi

    docker rmi "${IMAGE_NAME}:${VERSION}" 2>/dev/null || true
    exit 1
fi

log_success "Health check PASSED. Deployment successful: ${IMAGE_NAME}:${VERSION}"

log_info "Pruning image history (keeping last ${MAX_HISTORY} versions)..."

VERSIONS=$(docker images "${IMAGE_NAME}" --format "{{.Tag}}" \
    | grep -E '^v[0-9]{14}$' \
    | sort)

VERSION_COUNT=$(echo "${VERSIONS}" | grep -c '^v' 2>/dev/null || echo 0)

if [ "${VERSION_COUNT}" -gt "${MAX_HISTORY}" ]; then
    DELETE_COUNT=$((VERSION_COUNT - MAX_HISTORY))
    TO_DELETE=$(echo "${VERSIONS}" | head -n "${DELETE_COUNT}")

    for OLD_TAG in $TO_DELETE; do
        log_warn "Removing old image: ${IMAGE_NAME}:${OLD_TAG}"
        docker rmi "${IMAGE_NAME}:${OLD_TAG}" 2>/dev/null || true
    done
fi

log_success "Deployment complete! Active version: ${IMAGE_NAME}:${VERSION}"
echo ""
echo -e "${GREEN}=== Saved Image History ===${NC}"
docker images "${IMAGE_NAME}" --format "table {{.Tag}}\t{{.CreatedAt}}\t{{.Size}}" | grep -v "^TAG"