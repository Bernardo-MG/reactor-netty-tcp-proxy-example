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

## More Examples

This project is part of a series of examples:
- [Netty Proxy Example](https://github.com/Bernardo-MG/netty-proxy-example)
- [Reactor Netty Proxy Example](https://github.com/Bernardo-MG/reactor-netty-proxy-example)

But there are more Netty examples:
- [Netty TCP Client Example](https://github.com/Bernardo-MG/netty-tcp-client-example)
- [Reactor Netty TCP Client Example](https://github.com/Bernardo-MG/reactor-netty-tcp-client-example)
- [Netty TCP Server Example](https://github.com/Bernardo-MG/netty-tcp-server-example)
- [Reactor Netty TCP Server Example](https://github.com/Bernardo-MG/reactor-netty-tcp-server-example)
