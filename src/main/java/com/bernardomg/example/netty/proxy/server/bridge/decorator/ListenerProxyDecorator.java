
package com.bernardomg.example.netty.proxy.server.bridge.decorator;

import java.util.Objects;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import reactor.core.publisher.Flux;

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
