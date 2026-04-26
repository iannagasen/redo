
1. Install python

2. add python scripts to path
`C:\Users\ianna\AppData\Local\Python\pythoncore-3.14-64\Scripts`

3. Set Auth
`localstack set-auth <AUTH_TOKEN>`

4. Fix windows issue
```sh
[Environment]::SetEnvironmentVariable(
  "GATEWAY_LISTEN",
  "0.0.0.0:4566",
  "Machine"
)
```

5. start localstack
```
localstack start
```


---

# How to setup s3 in localstack

## Step 1: Create the deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: localstack
spec:
  replicas: 1
  selector:
    matchLabels:
      app: localstack
  template:
    metadata:
      labels:
        app: localstack
    spec:
      containers:
        - name: localstack
          image: localstack/localstack-pro:latest
          ports:
            - containerPort: 4566
          env:
            - name: SERVICES
              value: "s3"
            - name: AWS_DEFAULT_REGION
              value: "us-east-1"
            - name: LOCALSTACK_AUTH_TOKEN
              valueFrom:
                secretKeyRef:
                  name: localstack-secret
                  key: auth-token
```

## Step 2: Create the secret
```
apiVersion: v1
kind: Secret
metadata:
  name: localstack-secret
type: Opaque
stringData:
  auth-token: <your-token-here>
```

## Step 3: Make sure to add the secret in .gitignore

## Step 4: Create the service
```
apiVersion: v1
 kind: Service
 metadata:
   name: localstack
 spec:
   selector:
     app: localstack
   ports:
     - port: 4566
       targetPort: 4566
   type: ClusterIP
```

## Step 5: Create a 1 time batch that will create the bucket
```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: localstack-bucket-init
spec:
  template:
    spec:
      restartPolicy: OnFailure
      containers:
        - name: awscli
          image: amazon/aws-cli:latest
          env:
            - name: AWS_ACCESS_KEY_ID
              value: "test"
            - name: AWS_SECRET_ACCESS_KEY
              value: "test"
            - name: AWS_DEFAULT_REGION
              value: "us-east-1"
          command:
            - sh
            - -c
            - |
              echo "Waiting for LocalStack to be ready..."
              until aws --endpoint-url=http://localstack:4566 s3 ls > /dev/null 2>&1; do
                echo "LocalStack not ready yet, retrying in 3s..."
                sleep 3
              done
              echo "LocalStack is ready. Creating bucket..."
              aws --endpoint-url=http://localstack:4566 s3 mb s3://shopbuddy-products --region us-east-1
              echo "Done."
```

*For the Resource Browser* use firefox!!!! dont use chrome since it is blocking private network requests
URL: https://app.localstack.cloud