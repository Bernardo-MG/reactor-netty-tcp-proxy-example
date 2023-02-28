
package com.bernardomg.example.netty.proxy.server.bridge;

import java.util.Objects;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
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
    public final Disposable bridge(final Connection clientConn, final Connection serverConn) {
        final Disposable reqDispose;
        final Disposable respDispose;

        reqDispose = bindRequest(clientConn, serverConn);
        respDispose = bindResponse(clientConn, serverConn);

        return () -> {
            log.debug("Disposing bridge");
            reqDispose.dispose();
            respDispose.dispose();
        };
    }

    private final Disposable bindRequest(final Connection clientConn, final Connection serverConn) {
        log.debug("Binding request. Server inbound -> client outbound");

        return serverConn.inbound()
            .receive()
            .asString()
            .doOnCancel(() -> log.debug("Proxy server cancel"))
            .doOnComplete(() -> log.debug("Proxy server complete"))
            .doOnRequest((l) -> log.debug("Proxy server request"))
            .doOnEach((s) -> log.debug("Proxy server each"))
            .doOnNext((n) -> log.debug("Proxy server next"))
            .flatMap(next -> {
                log.debug("Handling request");

                // Sends the request to the listener

                log.debug("Server received request: {}", next);
                listener.onServerReceive(next);

                if (clientConn.isDisposed()) {
                    log.error("Client connection already disposed");
                }

                return clientConn.outbound()
                    .sendString(Mono.just(next)
                        .doOnNext((n) -> {
                            log.debug("Client sends request: {}", n);

                            listener.onClientSend(n);
                        }))
                    .then();
            })
            .doOnError(this::handleError)
            .subscribe();
    }

    private final Disposable bindResponse(final Connection clientConn, final Connection serverConn) {
        log.debug("Binding response. Client inbound -> server outbound");

        return clientConn.inbound()
            .receive()
            .asString()
            .doOnCancel(() -> log.debug("Proxy client cancel"))
            .doOnComplete(() -> log.debug("Proxy client complete"))
            .doOnRequest((l) -> log.debug("Proxy client request"))
            .doOnEach((s) -> log.debug("Proxy client each"))
            .doOnNext((n) -> log.debug("Proxy client next"))
            .flatMap(next -> {

                log.debug("Handling response");

                log.debug("Client received response: {}", next);
                listener.onClientReceive(next);

                if (serverConn.isDisposed()) {
                    log.error("Server connection already disposed");
                }

                return serverConn.outbound()
                    .sendString(Mono.just(next)
                        .doOnNext((n) -> {
                            log.debug("Server sends response: {}", n);

                            listener.onServerSend(n);
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
