/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.enforcer.deathstar.ws;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes the web socket connection
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger logger = Logger.getLogger(WebSocketServerInitializer.class.getName());
    private static final ChannelGroup allActiveChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * Creates the server initializer
     */
    public WebSocketServerInitializer() {
        logger.log(Level.FINE, "WebSocketServerInitializer created ", this.toString());
    }

    /**
     * Initializes each communication channel and sets up the
     * channel pipeline
     *
     * @param ch channel to initialize
     * @throws Exception if problems with channel setup
     */
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerHandler());
        allActiveChannels.add(ch);
    }

    /**
     * Broadcast a web socket message to all active clients
     *
     * @param message to be sent
     */
    public void broadcast(String message) {
        for(Channel channel : allActiveChannels) {
            channel.write(new TextWebSocketFrame(message));
        }
    }
}
