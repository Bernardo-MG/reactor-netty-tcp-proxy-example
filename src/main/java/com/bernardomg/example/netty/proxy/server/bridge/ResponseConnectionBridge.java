
package com.bernardomg.example.netty.proxy.server.bridge;

import java.util.Objects;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

@Slf4j
public final class ResponseConnectionBridge implements ConnectionBridge {

    private final ProxyListener listener;

    public ResponseConnectionBridge(final ProxyListener lst) {
        super();

        listener = Objects.requireNonNull(lst);
    }

    @Override
    public final Disposable bridge(final Connection clientConn, final Connection serverConn) {
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
