# Installation

1. Easiest, install minikube addon

```bash
minikube start
minikube addons enable ingress

kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx
# get the nodeport svc ip and add to /etc/host

```

2. 