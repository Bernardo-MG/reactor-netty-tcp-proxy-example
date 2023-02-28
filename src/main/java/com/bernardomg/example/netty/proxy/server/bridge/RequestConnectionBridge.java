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

package com.bernardomg.example.netty.proxy.server.bridge;

import java.util.Objects;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

/**
 * Bridge for binding requests. When the server receives a request through its inbound, this is redirected to the client
 * outbound.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class RequestConnectionBridge implements ConnectionBridge {

    /**
     * Proxy listener. Extension hook which allows reacting to the proxy events.
     */
    private final ProxyListener listener;

    public RequestConnectionBridge(final ProxyListener lst) {
        super();

        listener = Objects.requireNonNull(lst);
    }

    @Override
    public final Disposable bridge(final Connection clientConn, final Connection serverConn) {
        log.debug("Binding request. Server inbound -> client outbound");

        return serverConn
            // Receive
            .inbound()
            .receive()
            // Transform to byte array
            .asByteArray()
            // Logging
            .doOnCancel(() -> log.debug("Proxy server cancel"))
            .doOnComplete(() -> log.debug("Proxy server complete"))
            .doOnRequest((l) -> log.debug("Proxy server request"))
            .doOnEach((s) -> log.debug("Proxy server each"))
            .doOnNext((n) -> log.debug("Proxy server next"))
            // Process
            .flatMap(next -> {
                log.debug("Handling request");

                // Sends the request to the listener

                log.debug("Server received request: {}", next);
                listener.onServerReceive(next);

                if (clientConn.isDisposed()) {
                    log.error("Client connection already disposed");
                }

                return clientConn.outbound()
                    .sendByteArray(Mono.just(next)
                        .doOnNext((n) -> {
                            log.debug("Client sends request: {}", n);

                            listener.onClientSend(n);
                        }))
                    .then();
            })
            .doOnError(this::handleError)
            // Subscribe to run
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
