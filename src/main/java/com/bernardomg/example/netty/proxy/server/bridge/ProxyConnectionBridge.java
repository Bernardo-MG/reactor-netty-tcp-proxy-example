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
import java.util.function.Consumer;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

/**
 * Bridges connections to proxy requests and responses as if the proxy server was actually the target server. The end
 * result is that it builds two fluxes:
 * <ul>
 * <li>Request flux: {@code server inbound -> client outbound}</li>
 * <li>Response flux: {@code client inbound -> server outbound}</li>
 * </ul>
 * <h2>Disposing the bridge</h2>
 * <p>
 * Once the bridging is done both connections will share a single lifecycle. Actually, there will be a single lifecycle,
 * the server connection's. This will define when the other components are created and destroyed.
 * <p>
 * When the server connection is disposed of, then the following steps are taken:
 * <ul>
 * <li>Request flux is disposed of</li>
 * <li>Response flux is disposed of</li>
 * <li>Client channel is closed</li>
 * </ul>
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class ProxyConnectionBridge implements ConnectionBridge {

    /**
     * Proxy listener. Will received the requests.
     */
    private final ProxyListener listener;

    public ProxyConnectionBridge(final ProxyListener lstn) {
        super();

        listener = Objects.requireNonNull(lstn);
    }

    @Override
    public final void bridge(final Connection server, final Connection client) {
        final Disposable reqDispose;
        final Disposable respDispose;
        final Disposable bridgeDispose;

        log.debug("Binding request. Server inbound -> client outbound");
        reqDispose = decoratedBridge(server.inbound(), client.outbound(), listener::onRequest);

        log.debug("Binding response. Client inbound -> server outbound");
        respDispose = decoratedBridge(client.inbound(), server.outbound(), listener::onResponse);

        // Combines disposables
        // This includes closing the client channel
        bridgeDispose = Disposables.composite(reqDispose, respDispose, client.channel()::close);

        // When the server connection is disposed, so is the bridging
        server.onDispose(bridgeDispose);
    }

    /**
     * Bridges the connections, adding the decorator. This builds a flux which sends messages in the direction
     * {@code inbound -> outbound}.
     *
     * @param inbound
     *            source connection {@code NettyInbound}
     * @param outbound
     *            target connection {@code NettyOutbound}
     * @param decorator
     *            decorator to apply
     * @return disposable to get rid of the bridge flux
     */
    private final Disposable decoratedBridge(final NettyInbound inbound, final NettyOutbound outbound,
            final Consumer<? super ByteBuf> decorator) {
        return outbound.send(inbound.receive()
            .retain()
            .doOnNext(decorator))
            .then()
            .subscribe();
    }

}
