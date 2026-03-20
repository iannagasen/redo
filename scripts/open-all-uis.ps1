# Opens all ShopBuddy UIs in Chrome.
# Requires: minikube tunnel + skaffold dev already running.

$urls = @(
    "http://shopbuddy.com/storefront",
    "http://shopbuddy.com/k8s-dashboard",
    "http://shopbuddy.com/kafka-ui",
    "http://redis.shopbuddy.com",
    "http://shopbuddy.com/product/swagger-ui.html",
    "http://shopbuddy.com/cart/swagger-ui.html",
    "http://shopbuddy.com/order/swagger-ui.html",
    "http://shopbuddy.com/payment/swagger-ui.html",
    "https://shopbuddy.com/grafana"
)

Start-Process "chrome.exe" -ArgumentList ($urls -join " ")
