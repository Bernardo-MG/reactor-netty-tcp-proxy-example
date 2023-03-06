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
 * Bridge for binding requests. When the server receives a request through its inbound, this is redirected to the client
 * outbound.
 * <ul>
 * <li>Server inbound is redirected to client outbound</li>
 * </ul>
 * <h1>Listener</h1>
 * <p>
 * Additionally, the bridged connection will send requests to a {@link ProxyListener}.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class RequestConnectionBridge implements ConnectionBridge {

    /**
     * Proxy listener. Will received the requests.
     */
    private final ProxyListener listener;

    /**
     * Constructs a bridge with the received listener.
     *
     * @param lst
     *            proxy listener for the requests
     */
    public RequestConnectionBridge(final ProxyListener lst) {
        super();

        listener = Objects.requireNonNull(lst);
    }

    @Override
    public final Disposable bridge(final Connection clientConn, final Connection serverConn) {
        return serverConn
            // Receive
            .inbound()
            .receive()
            // Transform to byte array
            .asByteArray()
            // Process
            .flatMap(next -> {
                final Publisher<byte[]> dataStream;

                // Sends message to the listener
                listener.onServerReceive(next);

                if (clientConn.isDisposed()) {
                    log.error("Client connection already disposed");
                }

                dataStream = buildStream(next);

                return clientConn.outbound()
                    .sendByteArray(dataStream);
            })
            // Subscribe to run
            .subscribe();
    }

    /**
     * Builds a data stream for the received bytes.
     *
     * @param data
     *            byte array for the stream
     * @return data stream from the received bytes
     */
    private final Publisher<byte[]> buildStream(final byte[] data) {
        return Mono.just(data)
            .flux()
            .doOnNext(listener::onClientSend);
    }

}
