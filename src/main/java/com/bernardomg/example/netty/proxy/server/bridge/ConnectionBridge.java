
package com.bernardomg.example.netty.proxy.server.bridge;

import reactor.core.Disposable;
import reactor.netty.Connection;

public interface ConnectionBridge {

    public Disposable bridge(final Connection clientConn, final Connection serverConn);

}
