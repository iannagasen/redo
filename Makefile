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
all: rebuild k8s-up

# Step 0: Run infra services first (detached)
infra:
	docker compose -f $(INFRA_SERVICES) build --no-cache

# Step 1: Build Gradle project (skip tests for now)
gradle-build:
	$(GRADLE_WRAPPER) clean build -x test

# Step 2: Rebuild Docker image(s) using compose
rebuild: gradle-build
	docker compose -f $(CORE_SERVICES) build --no-cache

# Step 3: Start Minikube if not already running
k8s-start:
ifeq ($(OS),Windows_NT)
	@$(MINIKUBE_CMD) "if ((minikube status --format '{{.Host}}') -ne 'Running') { minikube start } else { Write-Host 'Minikube already running' }"
	minikube image load product:latest gateway:latest auth:latest
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
k8s-up: k8s-start rebuild tunnel-start
	kubectl apply -f $(K8S_MANIFESTS) --recursive
#ifeq ($(OS),Windows_NT)
#	@echo "Opening new PowerShell window for port-forwarding..."
#	cmd /c start powershell -NoExit -Command "minikube kubectl -- port-forward deployment/product-deployment 8080:8080"
#else
#	@echo "Opening new terminal for port-forwarding..."
#	@if [ "$(shell uname)" = "Darwin" ]; then \
#		osascript -e 'tell application "Terminal" to do script "minikube kubectl -- port-forward deployment/product-deployment 8080:8080"'; \
#	elif command -v gnome-terminal >/dev/null 2>&1; then \
#		gnome-terminal -- bash -c "minikube kubectl -- port-forward deployment/product-deployment 8080:8080; exec bash"; \
#	elif command -v xterm >/dev/null 2>&1; then \
#		xterm -e "minikube kubectl -- port-forward deployment/product-deployment 8080:8080; bash" & \
#	elif command -v konsole >/dev/null 2>&1; then \
#		konsole -e bash -c "minikube kubectl -- port-forward deployment/product-deployment 8080:8080; exec bash" & \
#	else \
#		echo "Could not detect terminal. Running in background..."; \
#		minikube kubectl -- port-forward deployment/product-deployment 8080:8080 & \
#	fi
#endif
#	@echo "Port-forward started in new terminal window"
