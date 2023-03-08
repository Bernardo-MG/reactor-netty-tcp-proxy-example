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

package com.bernardomg.example.netty.proxy.client;

import java.util.Objects;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

/**
 * Client for the proxy. This can create as many connections to the target server as needed. These are created
 * asynchronously, and returned inside a {@code Mono}.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class ReactorNettyProxyClient implements Client {

    /**
     * Host to which the proxy will connect.
     */
    private final String  host;

    /**
     * Port to which the proxy will connect.
     */
    private final Integer port;

    /**
     * Wiretap flag. Activates Reactor Netty wiretap logging.
     */
    @Setter
    @NonNull
    private Boolean       wiretap = false;

    /**
     * Constructs a client for the received host and port.
     *
     * @param hst
     *            host to connect to
     * @param prt
     *            port to connect to
     */
    public ReactorNettyProxyClient(final String hst, final Integer prt) {
        super();

        host = Objects.requireNonNull(hst);
        port = Objects.requireNonNull(prt);
    }

    @Override
    public final Mono<? extends Connection> connect() {
        log.trace("Starting proxy client");

        log.debug("Connecting to {}:{}", host, port);

        return TcpClient.create()
            // Wiretap
            .wiretap(wiretap)
            // Connect to target
            .host(host)
            .port(port)
            .connect();
    }

}
