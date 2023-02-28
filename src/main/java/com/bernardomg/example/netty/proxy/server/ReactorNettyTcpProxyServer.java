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
import java.util.Optional;

import org.reactivestreams.Publisher;

import com.bernardomg.example.netty.proxy.server.channel.EventLoggerChannelHandler;
import com.bernardomg.example.netty.proxy.server.channel.MessageListenerChannelInitializer;

import io.netty.util.CharsetUtil;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

/**
 * Netty based TCP server.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class ReactorNettyTcpProxyServer implements Server {

    private Optional<Connection> clientConnection = Optional.empty();

    private final ProxyListener  listener;

    /**
     * Port which the server will listen to.
     */
    private final Integer        port;

    private DisposableServer     server;

    private final String         targetHost;

    private final Integer        targetPort;

    /**
     * Wiretap flag.
     */
    @Setter
    @NonNull
    private Boolean              wiretap          = false;

    public ReactorNettyTcpProxyServer(final Integer prt, final String trgtHost, final Integer trgtPort,
            final ProxyListener lst) {
        super();

        port = Objects.requireNonNull(prt);
        targetHost = Objects.requireNonNull(trgtHost);
        targetPort = Objects.requireNonNull(trgtPort);
        listener = Objects.requireNonNull(lst);
    }

    @Override
    public final void start() {
        log.trace("Starting server");

        log.debug("Binding to port {}", port);

        listener.onStart();

        server = getServer();

        server.onDispose()
            .block();

        log.trace("Started server");
    }

    @Override
    public final void stop() {
        log.trace("Stopping server");

        clientConnection.get()
            .dispose();

        listener.onStop();

        server.dispose();

        log.trace("Stopped server");
    }

    private final void bindRequest(final Connection clientConn, final Connection serverConn) {
        serverConn.inbound()
            .receive()
            .doOnCancel(() -> log.debug("Proxy server cancel"))
            .doOnComplete(() -> log.debug("Proxy server complete"))
            .doOnRequest((l) -> log.debug("Proxy server request"))
            .doOnEach((s) -> log.debug("Proxy server each"))
            .doOnNext((n) -> log.debug("Proxy server next"))
            .flatMap(next -> {
                final String message;

                log.debug("Handling request");

                // Sends the request to the listener
                message = next.toString(CharsetUtil.UTF_8);

                log.debug("Received request: {}", message);
                return clientConn.outbound()
                    .sendString(Mono.just(message)
                        .flux());
            })
            .subscribe();
    }

    private final void bindResponse(final Connection clientConn, final Connection serverConn) {
        final ByteBufFlux request;

        request = clientConn.inbound()
            .receive();
        serverConn.outbound()
            .send(request);
    }

    private final Mono<? extends Connection> getClient() {
        log.trace("Starting client");

        log.debug("Proxy client connecting to {}:{}", targetHost, targetPort);

        return TcpClient.create()
            // Logs events
            .doOnChannelInit((o, c, a) -> log.debug("Proxy client channel init"))
            .doOnConnect(c -> log.debug("Proxy client connect"))
            .doOnConnected(c -> {
                log.debug("Proxy client connected");
                clientConnection = Optional.ofNullable(c);

                c.addHandlerLast(new EventLoggerChannelHandler("proxy client"));
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

    private final DisposableServer getServer() {
        return TcpServer.create()
            // Logs events
            .doOnChannelInit((o, c, a) -> log.debug("Server channel init"))
            .doOnConnection(c -> {
                log.debug("Server connection");
                c.addHandlerLast(new MessageListenerChannelInitializer("server"));
                getClient().subscribe((clientConn) -> {
                    log.debug("Binding connections");
                    bindRequest(clientConn, c);
                    bindResponse(clientConn, c);
                });
            })
            .doOnBind(c -> log.debug("Server bind"))
            .doOnBound(c -> log.debug("Server bound"))
            .doOnUnbound(c -> log.debug("Server unbound"))
            // Wiretap
            .wiretap(wiretap)
            // Adds request handler
            // .handle(this::handleServerRequest)
            // Binds to port
            .port(port)
            .bindNow();
    }

    /**
     * Error handler which sends errors to the log.
     *
     * @param ex
     *            exception to log
     */
    private final void handleError(final Throwable ex) {
        log.error(ex.getLocalizedMessage(), ex);
    }

    /**
     * Server request event listener. Will receive any request sent by the client, and then redirect it.
     * <p>
     * Additionally it will send the data from both the request and response to the listener.
     *
     * @param request
     *            request channel
     * @param response
     *            response channel
     * @return a publisher which handles the request
     */
    private final Publisher<Void> handleServerRequest(final NettyInbound request, final NettyOutbound response) {
        log.debug("Setting up request handler");

        // Receives the request and then sends a response
        return getClient().then(request.receive()
            .doOnCancel(() -> log.debug("Proxy client cancel"))
            .doOnComplete(() -> log.debug("Proxy client complete"))
            .doOnRequest((l) -> log.debug("Proxy client request"))
            .doOnEach((s) -> log.debug("Proxy client each"))
            .doOnNext((n) -> log.debug("Proxy client next"))
            // Handle request
            .flatMap(next -> {
                final String                  message;
                final Publisher<? extends String> dataStream;
                final NettyOutbound           clientRequest;
                final Publisher<Void>         clientResponse;

                log.debug("Handling request");

                // Sends the request to the listener
                message = next.toString(CharsetUtil.UTF_8);

                log.debug("Received request: {}", message);
                listener.onServerReceive(message);

                // Request data
                dataStream = Mono.just(message)
                    .flux()
                    // Will send the response to the listener
                    .doOnNext(s -> listener.onClientSend(s));

                // Redirect request to client outbound
                clientRequest = clientConnection.get()
                    .outbound()
                    .sendString(dataStream);

                // Intercept client inbound
                clientResponse = clientConnection.get()
                    .inbound()
                    .receive()
                    .flatMap(nxt -> {
                        final String msg;

                        msg = nxt.toString(CharsetUtil.UTF_8);
                        listener.onClientReceive(msg);

                        return response.sendString(Mono.just(msg))
                            .then();
                    });

                // Redirecto to client outbound then intercepts inbound
                return clientRequest.then(clientResponse);
            })
            // Cancel handler
            .doOnCancel(() -> {
                log.debug("Cancelled request. Sends back proxied response");
                clientConnection.get()
                    .inbound()
                    .receive()
                    .flatMap(nxt -> {
                        final String msg;

                        msg = nxt.toString(CharsetUtil.UTF_8);
                        listener.onClientReceive(msg);

                        return response.sendString(Mono.just(msg))
                            .then();
                    });
            })
            // Error handler
            .doOnError(this::handleError)
            .then());
    }

}
