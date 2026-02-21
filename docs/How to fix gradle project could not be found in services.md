**Delete the settings.gradle in that service**

```text
Build file 'C:\Users\ianna\Desktop\Ecommerce-redo\src\backend\infra\auth-server\build.gradle' line: 21
A problem occurred evaluating root project 'auth-server'.
> Project with path ':src:backend:platform:common' could not be found in root project 'auth-server'.
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.
BUILD FAILED in 103ms

but it has 

dependencies {
    implementation project(':src:backend:platform:common')
    implementation project(':src:backend:platform:api')
```