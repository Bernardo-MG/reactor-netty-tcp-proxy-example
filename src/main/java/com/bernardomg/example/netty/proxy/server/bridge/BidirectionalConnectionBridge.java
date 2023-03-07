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

import java.util.function.UnaryOperator;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.netty.Connection;

/**
 * Bridges requests on both directions, building a flux for requests and another for responses. It is actually composed
 * of two instances of {@link ProxyConnectionBridge}. This will bridge inbound and outbound fluxes to build two fluxes:
 * <ul>
 * <li>Request flux: {@code server inbound -> client outbound}</li>
 * <li>Response flux: {@code client inbound -> server outbound}</li>
 * </ul>
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class BidirectionalConnectionBridge implements ConnectionBridge {

    /**
     * Proxy request bridge.
     */
    private final ConnectionBridge requestConnectionBridge;

    /**
     * Proxy response bridge.
     */
    private final ConnectionBridge responseConnectionBridge;

    public BidirectionalConnectionBridge(final UnaryOperator<Flux<byte[]>> requestDecorator,
            final UnaryOperator<Flux<byte[]>> responseDecorator) {
        super();

        requestConnectionBridge = new ProxyConnectionBridge(requestDecorator);
        responseConnectionBridge = new ProxyConnectionBridge(responseDecorator);
    }

    @Override
    public final Disposable bridge(final Connection server, final Connection client) {
        final Disposable reqDispose;
        final Disposable respDispose;

        log.debug("Binding request. Server inbound -> client outbound");
        reqDispose = requestConnectionBridge.bridge(server, client);

        log.debug("Binding response. Client inbound -> server outbound");
        respDispose = responseConnectionBridge.bridge(client, server);

        // Combines disposables
        return Disposables.composite(reqDispose, respDispose);
    }

}
