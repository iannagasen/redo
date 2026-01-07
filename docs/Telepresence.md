```bash
telepresence status
telepresence connect
telepresence intercept <deployment-name> --port <host-app-port>:<container-port>
telepresence list
telepresence leave <deplyoment-name>

```

Write code
Build container
push to registry
deploy to cluster
test

above process can be slow

- context switch is slow

we need instant feedback

automation can help

- skaffold

skaffold - can sync java class now


---

What is telepresence

- "fancy kubernetes VPN for development"
- its kubectl --port-forward on steroids
- fundamentally a network bridge bw your machine and the k8s cluster

---

Good/Bad/Dont do

Benefits

- Use any tool that runs on your laptop: IDE, profiler, debugger
- Connect to cloud-based resources

Requirements

- Network connection needed
- need kubectl access to cluster
