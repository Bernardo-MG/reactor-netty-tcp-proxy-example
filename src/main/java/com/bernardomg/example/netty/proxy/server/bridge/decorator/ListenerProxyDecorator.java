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

package com.bernardomg.example.netty.proxy.server.bridge.decorator;

import java.util.Objects;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import reactor.core.publisher.Flux;

/**
 * Extends the proxy fluxes by calling a {@link ProxyListener} when requests or response are received.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
public final class ListenerProxyDecorator implements ProxyDecorator {

    /**
     * Proxy listener. Will received the requests.
     */
    private final ProxyListener listener;

    /**
     * Constructs an observer with the received listener.
     *
     * @param lst
     *            proxy listener for the requests
     */
    public ListenerProxyDecorator(final ProxyListener lst) {
        super();

        listener = Objects.requireNonNull(lst);
    }

    @Override
    public final Flux<byte[]> applyToRequest(final Flux<byte[]> flux) {
        return flux.doOnNext(listener::onRequest);
    }

    @Override
    public final Flux<byte[]> applyToResponse(final Flux<byte[]> flux) {
        return flux.doOnNext(listener::onResponse);
    }

}
