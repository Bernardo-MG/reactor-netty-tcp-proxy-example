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
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
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
    private Boolean              wiretap          = false;

    public ReactorNettyTcpProxyServer(final Integer prt, final String trgtHost, final Integer trgtPort,
            final ProxyListener lst) {
        super();

        port = Objects.requireNonNull(prt);
        targetHost = Objects.requireNonNull(trgtHost);
        targetPort = Objects.requireNonNull(trgtPort);
        listener = Objects.requireNonNull(lst);
    }

    public final void setWiretap(final Boolean wtap) {
        wiretap = wtap;
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

    private final Mono<? extends Connection> getClient() {
        log.trace("Starting client");

        log.debug("Client connecting to {}:{}", targetHost, targetPort);

        return TcpClient.create()
            // Logs events
            .doOnChannelInit((o, c, a) -> log.debug("Client channel init"))
            .doOnConnect(c -> log.debug("Client connect"))
            .doOnConnected(c -> {
                log.debug("Client connected");
                clientConnection = Optional.ofNullable(c);

                c.addHandlerLast(new EventLoggerChannelHandler("client"));
            })
            .doOnDisconnected(c -> log.debug("Client disconnected"))
            .doOnResolve(c -> log.debug("Client resolve"))
            .doOnResolveError((c, t) -> log.debug("Client resolve error"))
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
            })
            .doOnBind(c -> log.debug("Server bind"))
            .doOnBound(c -> log.debug("Server bound"))
            .doOnUnbound(c -> log.debug("Server unbound"))
            // Wiretap
            .wiretap(wiretap)
            // Adds request handler
            .handle(this::handleServerRequest)
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
     * Request event listener. Will receive any request sent by the client, and then send back the response.
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
            // Handle request
            .flatMap(next -> {
                final String                  message;
                final Publisher<? extends String> dataStream;

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
                // Sends request
                return clientConnection.get()
                    .outbound()
                    .sendString(dataStream)
                    .then(clientConnection.get()
                        .inbound()
                        .receive()
                        .flatMap(nxt -> {
                            final String msg;

                            msg = nxt.toString(CharsetUtil.UTF_8);
                            listener.onClientReceive(msg);

                            return response.sendString(Mono.just(msg))
                                .then();
                        }));
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
