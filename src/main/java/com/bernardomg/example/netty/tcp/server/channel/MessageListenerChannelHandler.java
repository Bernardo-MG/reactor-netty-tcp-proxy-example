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
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Message listener channel handler. Will send any message to the contained listener.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class MessageListenerChannelHandler extends SimpleChannelInboundHandler<String> {

    /**
     * Channel listener. This will receive any message from the channel.
     */
    private final BiConsumer<ChannelHandlerContext, String> listener;

    /**
     * Constructs a channel handler which will send any message to the listener.
     *
     * @param lstn
     *            listener to watch for channel messages
     */
    public MessageListenerChannelHandler(final BiConsumer<ChannelHandlerContext, String> lstn) {
        super();

        listener = Objects.requireNonNull(lstn);
    }

    @Override
    public final void channelRead0(final ChannelHandlerContext ctx, final String message) throws Exception {
        log.debug("Received message {}", message);

        listener.accept(ctx, message);
    }

}
