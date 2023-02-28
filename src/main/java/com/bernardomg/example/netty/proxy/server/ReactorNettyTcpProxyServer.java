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
import com.bernardomg.example.netty.proxy.server.channel.MessageListenerChannelInitializer;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableChannel;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

/**
 * Netty based TCP server. Will connect a Reactor Netty server and client, to redirect messages between the local port
 * and the remote URL being proxied.
 * <p>
 * The client and server send messages between each other. The server will receive any request, and then redirect them
 * to the client, which sends these requests to the proxied URL. This is done backwards for responses.
 * <p>
 * So requests go this way: {@code port -> Netty server -> Netty client -> proxied URL}, and responses work in reverse.
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
    public final void start() {
        log.trace("Starting server");

        log.debug("Binding to port {}", port);

        listener.onStart();

        server = connectoToServer();

        server.onDispose()
            .block();

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
        log.debug("Bridging connections");

        // Bridge to client connection
        connectToClient().subscribe((clientConn) -> {
            final Disposable bridgeDispose;

            log.debug("Bridging connection");

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
    private final DisposableServer connectoToServer() {
        return TcpServer.create()
            // Logs events
            .doOnChannelInit((o, c, a) -> log.debug("Server channel init"))
            .doOnConnection((c) -> c.addHandlerLast(new MessageListenerChannelInitializer("server")))
            .doOnConnection(this::bridgeConnections)
            .doOnBind(c -> log.debug("Server bind"))
            .doOnBound(c -> log.debug("Server bound"))
            .doOnUnbound(c -> log.debug("Server unbound"))
            // Wiretap
            .wiretap(wiretap)
            // Binds to port
            .port(port)
            .bindNow();
    }

    /**
     * Starts a client connection to the target URL, and returns a {@code Mono} to watch for. Once the client has
     * connected the {@code Mono} will contain the connection.
     *
     * @return {@code Mono} for the client connection
     */
    private final Mono<? extends Connection> connectToClient() {
        log.trace("Starting client");

        log.debug("Proxy client connecting to {}:{}", targetHost, targetPort);

        return TcpClient.create()
            // Logs events
            .doOnChannelInit((o, c, a) -> log.debug("Proxy client channel init"))
            .doOnConnect(c -> log.debug("Proxy client connect"))
            .doOnConnected(c -> {
                log.debug("Proxy client connected");
                c.addHandlerLast(new MessageListenerChannelInitializer("proxy client"));
            })
            .doOnDisconnected(c -> log.debug("Proxy client disconnected"))
            .doOnResolve(c -> log.debug("Proxy client resolve"))
            .doOnResolveError((c, t) -> log.debug("Proxy client resolve error"))
            // Wiretap
            .wiretap(wiretap)
            // Sets connection
            .host(targetHost)
            .port(targetPort)
            .connect();
    }

}
