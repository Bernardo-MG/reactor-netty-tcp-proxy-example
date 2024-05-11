# Reactor Netty TCP Proxy Example

A small Reactor Netty proxy server to serve as an example.

To use the project first package it:

```
mvn clean package
```

The JAR will be a runnable Java file. It can be executed like this:

```
java -jar target/proxy.jar start --port=9090 --targetHost=localhost --targetPort=8080
```

To show other commands:

```
java -jar target/proxy.jar -h
```

## Other Netty examples

### TCP

- [Netty TCP Client Example](https://github.com/Bernardo-MG/netty-tcp-client-example)
- [Netty TCP Server Example](https://github.com/Bernardo-MG/netty-tcp-server-example)
- [Netty TCP Proxy Example](https://github.com/Bernardo-MG/netty-tcp-proxy-example)

### TCP Reactive

- [Reactor Netty TCP Client Example](https://github.com/Bernardo-MG/reactor-netty-tcp-client-example)
- [Reactor Netty TCP Server Example](https://github.com/Bernardo-MG/reactor-netty-tcp-server-example)
- [Reactor Netty TCP Proxy Example](https://github.com/Bernardo-MG/reactor-netty-tcp-proxy-example)

### HTTP Reactive

- [Reactor Netty HTTP Client Example](https://github.com/Bernardo-MG/reactor-netty-http-client-example)
- [Reactor Netty HTTP Server Example](https://github.com/Bernardo-MG/reactor-netty-http-server-example)

## Features

- Reactor Netty proxy
- Command Line Client

## References

- [Netty-Simple-UDP-TCP-server-client](https://github.com/narkhedesam/Netty-Simple-UDP-TCP-server-client)

## Documentation

The documentation site is actually a Maven site, its sources are included in the project. Can be generated by using the following Maven command:

```
mvn verify site
```

The verify phase is required, otherwise some of the reports won't be built.

## Collaborate

Any kind of help with the project will be well received, and there are two main ways to give such help:

- Reporting errors and asking for extensions through the issues management
- or forking the repository and extending the project

### Issues management

Issues are managed at the GitHub [project issues tracker][issues], where any Github user may report bugs or ask for new features.

### Getting the code

If you wish to fork or modify the code, visit the [GitHub project page][scm], where the latest versions are always kept. Check the 'master' branch for the latest release, and the 'develop' for the current, and stable, development version.

## License

The project has been released under the [MIT License][license].

[issues]: https://github.com/bernardo-mg/reactor-netty-tcp-proxy-example/issues
[license]: https://www.opensource.org/licenses/mit-license.php
[scm]: https://github.com/bernardo-mg/reactor-netty-tcp-proxy-example
