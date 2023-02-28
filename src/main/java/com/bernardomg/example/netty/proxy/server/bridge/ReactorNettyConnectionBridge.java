
package com.bernardomg.example.netty.proxy.server.bridge;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.netty.Connection;

@Slf4j
public final class ReactorNettyConnectionBridge implements ConnectionBridge {

    private final ConnectionBridge requestConnectionBridge;

    private final ConnectionBridge responseConnectionBridge;

    public ReactorNettyConnectionBridge(final ProxyListener lst) {
        super();

        requestConnectionBridge = new RequestConnectionBridge(lst);
        responseConnectionBridge = new ResponseConnectionBridge(lst);
    }

    @Override
    public final Disposable bridge(final Connection clientConn, final Connection serverConn) {
        final Disposable reqDispose;
        final Disposable respDispose;

        reqDispose = requestConnectionBridge.bridge(clientConn, serverConn);
        respDispose = responseConnectionBridge.bridge(clientConn, serverConn);

        return () -> {
            log.debug("Disposing bridge");
            reqDispose.dispose();
            respDispose.dispose();
        };
    }

}
