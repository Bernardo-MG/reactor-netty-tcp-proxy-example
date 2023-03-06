# Usage example

Once built, the JAR will be located at target/proxy.jar. These examples will use said path.

## Commands

### Help

The CLI includes a help option, which shows commands:

```
java -jar target/proxy.jar -h
```

This extends to the commands, showing arguments and options:

```
java -jar target/proxy.jar start -h
```

### Start Server

To start the proxy and listen to port 9090, for redirecting requests to localhost:8080:

```
java -jar target/proxy.jar start 9090 localhost 8080
```
