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
import java.util.function.UnaryOperator;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

/**
 * Builds a proxy between two connections, using one as inbound and the other as outbound. Any data received by the
 * inbound will be sent to the outbound. This will generate a flux, which is returned as a {@code Disposable}.
 * <p>
 * Additionally, it will apply a decorator to extend said proxied flux.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
public final class ProxyConnectionBridge implements ConnectionBridge {

    private final UnaryOperator<Flux<byte[]>> decorator;

    /**
     * Constructs a bridge with the received listener.
     *
     * @param dec
     *            flux decorator
     */
    public ProxyConnectionBridge(final UnaryOperator<Flux<byte[]>> dec) {
        super();

        decorator = Objects.requireNonNull(dec);
    }

    @Override
    public final Disposable bridge(final Connection inbound, final Connection outbound) {
        final Flux<byte[]> flux;
        final Flux<byte[]> decorated;

        flux = inbound
            // Receive
            .inbound()
            .receive()
            // Transform to byte array
            .asByteArray();

        // Applies decorator
        decorated = decorator.apply(flux);

        return decorated
            // proxy
            .flatMap(next -> {
                return outbound.outbound()
                    .sendByteArray(Mono.just(next));
            })
            // Subscribe to run
            .subscribe();
    }

}