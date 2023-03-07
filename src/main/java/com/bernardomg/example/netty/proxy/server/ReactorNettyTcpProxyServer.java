/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2023 the original author or authors.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bernardomg.example.netty.proxy.server;

import java.util.Objects;

import com.bernardomg.example.netty.proxy.server.bridge.BidirectionalConnectionBridge;
import com.bernardomg.example.netty.proxy.server.bridge.ConnectionBridge;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableChannel;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

/**
 * Netty based TCP proxy. With the user of a Reactor Netty server and clients, it will redirect all connections to the
 * target URL.
 * <h2>Connection bridging</h2>
 * <p>
 * When the server starts a new connection, then a new client is started for said server connection. They are connected
 * through a {@link BidirectionalConnectionBridge}, which will redirect request and response streams between them. So
 * requests go this way: {@code listened port -> Netty server -> Netty client -> proxied URL}, and responses work in
 * reverse.
 * <p>
 * This also means than for each proxy server there may exist multiple clients. As many as current requests.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class ReactorNettyTcpProxyServer implements Server {

    /**
     * Connection bridge to connect the proxy server and clients.
     */
    private final ConnectionBridge bridge;

    /**
     * Proxy listener. Extension hook which allows reacting to the proxy events.
     */
    private final ProxyListener    listener;

    /**
     * Port which the server will listen to.
     */
    private final Integer          port;

    /**
     * Disposable for closing the server port connection.
     */
    private DisposableChannel      server;

    /**
     * Host to which the proxy will connect.
     */
    private final String           targetHost;

    /**
     * Port to which the proxy will connect.
     */
    private final Integer          targetPort;

    /**
     * Wiretap flag. Activates Reactor Netty wiretap logging.
     */
    @Setter
    @NonNull
    private Boolean                wiretap = false;

    /**
     * Constructs a proxy server redirecting the received port to the target URL.
     *
     * @param prt
     *            port to listen to
     * @param trgtHost
     *            target host
     * @param trgtPort
     *            target port
     * @param lst
     *            proxy listener
     */
    public ReactorNettyTcpProxyServer(final Integer prt, final String trgtHost, final Integer trgtPort,
            final ProxyListener lst) {
        super();

        port = Objects.requireNonNull(prt);
        targetHost = Objects.requireNonNull(trgtHost);
        targetPort = Objects.requireNonNull(trgtPort);
        listener = Objects.requireNonNull(lst);
        bridge = new BidirectionalConnectionBridge(listener);
    }

    @Override
    public final void listen() {
        log.trace("Starting server listening");

        server.onDispose()
            .block();

        log.trace("Stopped server listening");
    }

    @Override
    public final void start() {
        log.trace("Starting server");

        log.debug("Binding to port {}", port);

        server = connectoToServer();

        log.trace("Started server");
    }

    @Override
    public final void stop() {
        log.trace("Stopping server");

        listener.onStop();

        server.dispose();

        log.trace("Stopped server");
    }

    /**
     * Bridges the server and client connections.
     *
     * @param serverConn
     *            server connection
     */
    private final void bridgeConnections(final Connection serverConn) {
        // Connect to client, and wait for connection to be available
        connectToClient().subscribe((clientConn) -> {
            final Disposable bridgeDispose;

            log.debug("Bridging connection with {}", bridge);

            bridgeDispose = bridge.bridge(clientConn, serverConn);

            // When the server connection is disposed, so is the bridging
            serverConn.onDispose(bridgeDispose);
        });
    }

    /**
     * Starts a server connection and returns a disposable.
     *
     * @return disposable for disposing the server
     */
    private final DisposableChannel connectoToServer() {
        return TcpServer.create()
            // Bridge connection
            .doOnConnection(this::bridgeConnections)
            // Listen to events
            .doOnBind(c -> listener.onStart())
            // Wiretap
            .wiretap(wiretap)
            // Bind to port
            .port(port)
            .bindNow()
            // Listen to events
            .onDispose(() -> listener.onStop());
    }

    /**
     * Starts a client connection to the target URL, and returns a {@code Mono} to watch for. Once the client has
     * connected the {@code Mono} will contain the connection.
     *
     * @return {@code Mono} for the client connection
     */
    private final Mono<? extends Connection> connectToClient() {
        log.trace("Starting proxy client");

        log.debug("Proxy client connecting to {}:{}", targetHost, targetPort);

        return TcpClient.create()
            // Wiretap
            .wiretap(wiretap)
            // Connect to target
            .host(targetHost)
            .port(targetPort)
            .connect();
    }

}
