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

package com.bernardomg.example.netty.tcp.server.channel;

import java.util.Objects;
import java.util.function.BiConsumer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes the channel with a message listener. Any message received by the channel will be sent to the listener.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class MessageListenerChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * Message listener. This will receive any response from the channel.
     */
    private final BiConsumer<ChannelHandlerContext, String> listener;

    public MessageListenerChannelInitializer(final BiConsumer<ChannelHandlerContext, String> lstn) {
        super();

        listener = Objects.requireNonNull(lstn);
    }

    @Override
    protected final void initChannel(final SocketChannel ch) throws Exception {
        final MessageListenerChannelHandler listenerHandler;

        // Message listener handler
        // Sends any message received by the channel to the listener
        listenerHandler = new MessageListenerChannelHandler(listener);

        log.debug("Initializing channel");

        ch.pipeline()
            // Transforms message into a string
            .addLast("decoder", new StringDecoder())
            // Adds event logger
            .addLast(new EventLoggerChannelHandler())
            // Adds listener handler
            .addLast(listenerHandler);

        log.debug("Initialized channel");
    }

}
