# ARGUMENT
EXCLUDE ?=
SERVICES := product gateway auth storefront
FILTERED := $(filter-out $(EXCLUDE),$(SERVICES))

# Variables
CORE_SERVICES=docker-compose-build-core-services.yaml
INFRA_SERVICES=docker-compose-infra.yaml
K8S_MANIFESTS=k8s/

# Detect OS for Gradle wrapper
ifeq ($(OS),Windows_NT)
	GRADLE_WRAPPER=gradlew.bat
	MINIKUBE_CMD=powershell -NoProfile -Command
else
	GRADLE_WRAPPER=./gradlew
	MINIKUBE_CMD=
endif

# Default target
all: infra rebuild k8s-up

# Step 0: Run infra services first (detached)
infra:
	docker compose -f $(INFRA_SERVICES) build --no-cache

# Step 1: Build Gradle project (skip tests for now)
gradle-build:
	$(GRADLE_WRAPPER) clean build -x test

# Step 2: Rebuild Docker image(s) using compose
rebuild: gradle-build
	@echo "Building: $(FILTERED)"
	docker compose -f $(CORE_SERVICES) build --no-cache $(FILTERED)

# Step 3: Start Minikube if not already running
k8s-start:
ifeq ($(OS),Windows_NT)
	@$(MINIKUBE_CMD) "if ((minikube status --format '{{.Host}}') -ne 'Running') { minikube start } else { Write-Host 'Minikube already running' }"
	minikube image load product:latest gateway:latest auth:latest storefront:latest
else
	@STATUS=$$(minikube status --format "{{.Host}}"); \
	if [ "$$STATUS" != "Running" ]; then \
		echo "Starting Minikube..."; \
		minikube start; \
	else \
		echo "Minikube already running"; \
	fi
endif

tunnel-start:
	@powershell -Command "Start-Process powershell -Verb RunAs -ArgumentList '-NoExit', '-Command', 'minikube tunnel'"

# Step 4: Deploy to Kubernetes
# I dont even understand the ifeq part ... 🤯
# but the idea is, open a new terminal and port forward
k8s-up: k8s-start
ifeq ($(EXCLUDE),storefront)
	@echo "Skipping storefront manifest..."
	kubectl apply -f $(K8S_MANIFESTS) --recursive --selector 'app!=storefront'
else
	kubectl apply -f $(K8S_MANIFESTS) --recursive
endif

install-cert-manager:
	@echo "Installing cert-manager"
	powershell -NoProfile -Command "if (-not (kubectl get secret shopbuddy-mkcert-tls -n default -o json)) { kubectl create secret tls shopbuddy-mkcert-tls --cert=k8s/shopbuddy.com.dummy.pem --key=k8s/shopbuddy.com-key.pem -n default }"
	kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.1/cert-manager.crds.yaml
	powershell -NoProfile -Command "if (-not (kubectl get namespace cert-manager -o json)) { kubectl create namespace cert-manager }"
	kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.1/cert-manager.yaml


