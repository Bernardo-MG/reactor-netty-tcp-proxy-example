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

package com.bernardomg.example.netty.proxy.cli;

import java.io.PrintWriter;
import java.util.Objects;

import com.bernardomg.example.netty.proxy.server.ProxyListener;

/**
 * Proxy listener which will write the context of each step into the CLI console.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
public final class CliWriterProxyListener implements ProxyListener {

    /**
     * Port which the proxy will listen to.
     */
    private final Integer     port;

    /**
     * Host to which the proxy will connect.
     */
    private final String      targetHost;

    /**
     * Port to which the proxy will connect.
     */
    private final Integer     targetPort;

    /**
     * CLI writer, to print console messages.
     */
    private final PrintWriter writer;

    public CliWriterProxyListener(final Integer prt, final String trgtHost, final Integer trgtPort,
            final PrintWriter writ) {
        super();

        port = Objects.requireNonNull(prt);
        targetHost = Objects.requireNonNull(trgtHost);
        targetPort = Objects.requireNonNull(trgtPort);
        writer = Objects.requireNonNull(writ);
    }

    @Override
    public final void onRequest(final Object message) {
        writer.printf("Received request message: %s", message);
        writer.println();
    }

    @Override
    public final void onResponse(final Object message) {
        writer.printf("Received response message: %s", message);
        writer.println();
    }

    @Override
    public final void onStart() {
        writer.printf("Redirecting port %d to %s:%d", port, targetHost, targetPort);
        writer.println();
    }

    @Override
    public final void onStop() {
        writer.println("Stopping connection");
    }

}
