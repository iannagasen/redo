#!/bin/bash

# Opens all ShopBuddy UIs in the default browser.
# Requires: minikube tunnel + skaffold dev already running.

urls=(
  "http://shopbuddy.com/storefront"
  "http://shopbuddy.com/k8s-dashboard"
  "http://shopbuddy.com/kafka-ui"
  "http://redis.shopbuddy.com"
  "http://shopbuddy.com/product/swagger-ui.html"
  "http://shopbuddy.com/cart/swagger-ui.html"
  "http://shopbuddy.com/order/swagger-ui.html"
  "http://shopbuddy.com/payment/swagger-ui.html"
)

for url in "${urls[@]}"; do
  echo "Opening $url"
  explorer.exe "$url"
  sleep 0.5
done
