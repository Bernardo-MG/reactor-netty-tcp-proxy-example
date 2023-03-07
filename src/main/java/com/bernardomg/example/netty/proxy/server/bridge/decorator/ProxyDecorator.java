
package com.bernardomg.example.netty.proxy.server.bridge.decorator;

import reactor.core.publisher.Flux;

public interface ProxyDecorator {

    public Flux<byte[]> applyToRequest(final Flux<byte[]> flux);

    public Flux<byte[]> applyToResponse(final Flux<byte[]> flux);

}
