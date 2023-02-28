
package com.bernardomg.example.netty.proxy.server.bridge;

import java.util.Objects;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

@Slf4j
public final class ReactorNettyConnectionBridge implements ConnectionBridge {

    private final ProxyListener listener;

    public ReactorNettyConnectionBridge(final ProxyListener lst) {
        super();

        listener = Objects.requireNonNull(lst);
    }

    @Override
    public final void bridge(final Connection clientConn, final Connection serverConn) {
        bindRequest(clientConn, serverConn);
        bindResponse(clientConn, serverConn);
    }

    private final void bindRequest(final Connection clientConn, final Connection serverConn) {
        log.debug("Binding request. Server inbound -> client outbound");

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

                log.debug("Server received request: {}", message);
                listener.onServerReceive(message);

                return clientConn.outbound()
                    .send(Mono.just(next)
                        .doOnNext((n) -> {
                            final String msg;

                            msg = n.toString(CharsetUtil.UTF_8);

                            log.debug("Client sends request: {}", msg);

                            listener.onClientSend(msg);
                        }));
            })
            .doOnError(this::handleError)
            .subscribe();
    }

    private final void bindResponse(final Connection clientConn, final Connection serverConn) {
        log.debug("Binding response. Client inbound -> server outbound");

        clientConn.inbound()
            .receive()
            .doOnCancel(() -> log.debug("Proxy client cancel"))
            .doOnComplete(() -> log.debug("Proxy client complete"))
            .doOnRequest((l) -> log.debug("Proxy client request"))
            .doOnEach((s) -> log.debug("Proxy client each"))
            .doOnNext((n) -> log.debug("Proxy client next"))
            .flatMap(next -> {
                final String message;

                log.debug("Handling response");

                // Sends the request to the listener
                message = next.toString(CharsetUtil.UTF_8);

                log.debug("Client received response: {}", message);
                listener.onClientReceive(message);

                return serverConn.outbound()
                    .send(Mono.just(next)
                        .doOnNext((n) -> {
                            final String msg;

                            msg = n.toString(CharsetUtil.UTF_8);

                            log.debug("Server sends response: {}", msg);

                            listener.onServerSend(msg);
                        }));
            })
            .doOnError(this::handleError)
            .subscribe();
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

}
