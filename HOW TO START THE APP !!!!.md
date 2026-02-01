# Creating this because I always forget how to start after leaving it for a long time

Previous iterations you need to use make, until I discovered Skaffold

# Steps in starting the backend architecture

```sh
# go to repo root directory 

minikube start
minikube tunnel
skaffold dev
```

urls:
_note: hosts file should be updated to have the shopbuddy.com domain_
https://shopbuddy.com/product/swagger-ui/index.html#/product-controller/addProduct

# Steps in starting the frontend

```sh
# go to /src/frontend/storefront
ng serve
```

urls:
http://localhost:4200/storefront/dashboard

