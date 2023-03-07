package com.bernardomg.example.netty.proxy.server.observer;

import reactor.core.publisher.Flux;

public interface ProxyObserver {
    
    public void setRequestFlux(final Flux<byte[]> flux);
    public void setResponseFlux(final Flux<byte[]> flux);

}
