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

import org.reactivestreams.Publisher;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

/**
 * Bridge for binding responses. When the client receives a response through its inbound, this is redirected to the
 * server outbound.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class ResponseConnectionBridge implements ConnectionBridge {

    /**
     * Proxy listener. Extension hook which allows reacting to the proxy events.
     */
    private final ProxyListener listener;

    public ResponseConnectionBridge(final ProxyListener lst) {
        super();

        listener = Objects.requireNonNull(lst);
    }

    @Override
    public final Disposable bridge(final Connection clientConn, final Connection serverConn) {
        return clientConn
            // Receive
            .inbound()
            .receive()
            // Transform to byte array
            .asByteArray()
            // Process
            .flatMap(next -> {
                final Publisher<byte[]> dataStream;

                log.debug("Handling response");

                log.debug("Client received response: {}", next);

                // Sends message to the listener
                listener.onClientReceive(next);

                if (serverConn.isDisposed()) {
                    log.error("Server connection already disposed");
                }

                dataStream = buildStream(next);

                return serverConn.outbound()
                    .sendByteArray(dataStream);
            })
            .doOnError(this::handleError)
            // Subscribe to run
            .subscribe();
    }

    private final Publisher<byte[]> buildStream(final byte[] next) {
        return Mono.just(next)
            .flux()
            .doOnNext(m -> {
                // Sends the message to the listener
                listener.onServerSend(m);
            });
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
