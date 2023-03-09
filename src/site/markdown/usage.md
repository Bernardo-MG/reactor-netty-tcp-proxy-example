# Usage example

Once built, the JAR will be located at target/proxy.jar. These examples will use said path.

## Commands

### Start Server

To start the proxy and listen to port 9090, for redirecting requests to localhost:8080:

```
java -jar target/proxy.jar start --port=9090 --targetHost=localhost --targetPort=8080
```

## Help

The CLI includes a help option, which shows commands:

```
java -jar target/proxy.jar -h
```

This extends to the commands, showing arguments and options:

```
java -jar target/proxy.jar start -h
```

## Debug

All the commands have a debug option, which prints logs on console:

```
java -jar target/proxy.jar start --port=9090 --targetHost=localhost --targetPort=8080 --debug
```

This includes details on all the messages sent or received.
