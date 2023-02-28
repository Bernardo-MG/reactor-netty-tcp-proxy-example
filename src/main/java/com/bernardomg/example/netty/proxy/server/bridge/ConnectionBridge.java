
package com.bernardomg.example.netty.proxy.server.bridge;

import reactor.netty.Connection;

public interface ConnectionBridge {

    public void bridge(final Connection clientConn, final Connection serverConn);

}
