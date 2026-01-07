```bash
# check what manifests to render
skaffold render

skaffold dev

```

# Source: https://gist.github.com/0466cff59d08cabb5276f593ca2e513d

#########################################

# Skaffold                              #

# How to Build and Deploy In Kubernetes #

# https://youtu.be/qS_4Qf8owc0          #

#########################################

#########

# Setup #

#########

# Install `skaffold` CLI from https://skaffold.dev/docs/install/

minikube start

minikube addons enable ingress

# Change the value to whatever is your Ingress host if not using minikube

export INGRESS_HOST=$(minikube ip).nip.io

git clone https://github.com/vfarcic/skaffold-demo.git

cd skaffold-demo

# This is just in case I forgot to remove it from the repo.

rm -rf skaffold.yaml

cat orig/ingress.yaml \
| sed -e "s@acme.com@dev.devops-toolkit.$INGRESS_HOST@g" \
| tee k8s/ingress.yaml

cat kustomize/base/ingress-patch.yaml \
| sed -e "s@acme.com@devops-toolkit.$INGRESS_HOST@g" \
| tee kustomize/overlays/production/ingress-patch.yaml

##############

# Initialize #

##############

cat Dockerfile

skaffold init --help

skaffold init

cat skaffold.yaml

###########

# Develop #

###########

cp skaffold-simple.yaml skaffold.yaml

skaffold dev

# Switch to a new terminal session

kubectl get all

kubectl get ingress

# Open the app in a browser

# Feel free to use any other editor

vim config.toml

# Go back to the terminal session where `skaffold` is running

# Cancel with *ctrl+c*

kubectl get all,ingress

###################

# Supported tools #

###################

# Open https://skaffold.dev/docs/references/yaml

##################

# Build & deploy #

##################

cp skaffold-better.yaml skaffold.yaml

cat skaffold.yaml

skaffold run --profile prod

kubectl --namespace production \
get all,ingress

skaffold delete \
--profile prod

skaffold build

skaffold build --quiet

skaffold build --quiet \
| skaffold deploy \
--build-artifacts -

skaffold delete

skaffold render