
package com.bernardomg.example.netty.proxy.server.observer;

import java.util.Objects;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

import reactor.core.publisher.Flux;

public final class ListenerProxyObserver implements ProxyObserver {

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
    public ListenerProxyObserver(final ProxyListener lst) {
        super();

        listener = Objects.requireNonNull(lst);
    }

    @Override
    public final void setRequestFlux(final Flux<byte[]> flux) {
        flux.doOnNext(listener::onRequest);
    }

    @Override
    public final void setResponseFlux(final Flux<byte[]> flux) {
        flux.doOnNext(listener::onResponse);
    }

}
