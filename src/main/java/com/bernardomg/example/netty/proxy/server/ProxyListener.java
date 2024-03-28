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

package com.bernardomg.example.netty.proxy.server;

import io.netty.buffer.ByteBuf;

/**
 * Proxy transaction listener. Allows reacting to the events of a proxied connection.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
public interface ProxyListener {

    /**
     * Reacts to a request message being received by the server from the client.
     *
     * @param message
     *            request message received
     */
    public void onRequest(final ByteBuf message);

    /**
     * Reacts to a response message being sent by the server to the client.
     *
     * @param message
     *            response message sent
     */
    public void onResponse(final ByteBuf message);

    /**
     * Reacts to the start event.
     */
    public void onStart();

    /**
     * Reacts to the stop event.
     */
    public void onStop();

}
