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

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.netty.Connection;

/**
 * Bridge for binding both requests and responses. Is composed of request and response bridges, which will take care of
 * the bridging:
 * <ul>
 * <li>Server inbound is redirected to client outbound</li>
 * <li>Client inbound is redirected to server outbound</li>
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

    public BidirectionalConnectionBridge(final ProxyListener lst) {
        super();

        requestConnectionBridge = new RequestConnectionBridge(lst);
        responseConnectionBridge = new ResponseConnectionBridge(lst);
    }

    @Override
    public final Disposable bridge(final Connection clientConn, final Connection serverConn) {
        final Disposable reqDispose;
        final Disposable respDispose;

        log.debug("Binding request. Server inbound -> client outbound");
        reqDispose = requestConnectionBridge.bridge(clientConn, serverConn);

        log.debug("Binding response. Client inbound -> server outbound");
        respDispose = responseConnectionBridge.bridge(clientConn, serverConn);

        return Disposables.composite(reqDispose, respDispose);
    }

}
