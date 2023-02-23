# Reactor Netty Proxy Example

A small Reactor Netty proxy server to serve as an example.

To use the project first package it:

```
mvn clean package
```

The JAR will be a runnable Java file. It can be executed like this:

```
java -jar target/proxy.jar tcp 9090 localhost 8080
```

To show other commands:

```
java -jar target/proxy.jar -h
```
