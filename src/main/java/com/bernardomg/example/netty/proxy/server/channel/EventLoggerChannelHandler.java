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

package com.bernardomg.example.netty.proxy.server.channel;

import java.util.Objects;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Event logger adapter. Will log each event.
 *
 * @author Bernardo Mart&iacute;nez Garrido
 *
 */
@Slf4j
public final class EventLoggerChannelHandler extends ChannelInboundHandlerAdapter {

    /**
     * Source name.
     */
    private final String name;

    public EventLoggerChannelHandler(final String nm) {
        super();

        name = Objects.requireNonNull(nm);
    }

    @Override
    public final void channelActive(final ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel active for {}", name);
    }

    @Override
    public final void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel inactive for {}", name);
    }

    @Override
    public final void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        log.debug("Channel read for {}", name);
    }

    @Override
    public final void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel read complete for {}", name);
    }

    @Override
    public final void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel registered for {}", name);
    }

    @Override
    public final void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel unregistered for {}", name);
    }

    @Override
    public final void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        log.debug("User event triggered for {}", name);
    }

}
