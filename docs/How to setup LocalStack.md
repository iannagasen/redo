
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