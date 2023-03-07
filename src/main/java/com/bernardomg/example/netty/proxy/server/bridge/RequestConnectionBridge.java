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
import com.bernardomg.example.netty.proxy.server.observer.ProxyObserver;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

/**
 * Bridges the proxy server requests to the proxy client, creating a single request flux. This means redirecting the
 * proxy server inbound to the proxy client outbound.
 * <h2>Listener</h2>
 * <p>
 * Additionally, the bridged connection will send requests to a {@link ProxyListener}.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
public final class RequestConnectionBridge implements ConnectionBridge {

    private final ProxyObserver observer;

    /**
     * Constructs a bridge with the received listener.
     *
     * @param obsv
     *            proxy observer
     */
    public RequestConnectionBridge(final ProxyObserver obsv) {
        super();

        observer = Objects.requireNonNull(obsv);
    }

    @Override
    public final Disposable bridge(final Connection clientConn, final Connection serverConn) {
        final Flux<byte[]> flux;

        flux = serverConn
            // Receive
            .inbound()
            .receive()
            // Transform to byte array
            .asByteArray();

        observer.setRequestFlux(flux);

        return flux
            // proxy
            .flatMap(next -> {
                return clientConn.outbound()
                    .sendByteArray(Mono.just(next));
            })
            // Subscribe to run
            .subscribe();
    }

}
