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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modified version of the Netty WebSocket example. Added the
 * ability to broadcast to all connected clients.
 *
 */
public final class WebSocketServer implements Runnable {

    /**
     * Class logger
     */
    private static final Logger logger = Logger.getLogger(WebSocketServer.class.getName());

    /**
     * Port on which the WebSocket server will run
     */
    private final Integer port;

    /**
     * Initializes the netty channel using handlers
     */
    private final WebSocketServerInitializer webSocketServerInitializer;

    /**
     * Creates an instance of the web socket server on the specified port
     *
     * @param port used for web socket
     */
    public WebSocketServer(Integer port) {
        this.port = port;
        this.webSocketServerInitializer = new WebSocketServerInitializer();
        logger.log(Level.FINE, "webSocket server instantiated {0} ", this);
    }

    /**
     * Sends a message to all active channels
     *
     * @param message to be sent to all connected websocket clients
     */
    public void broadcastToAllWebSocketClients(String message) {
        webSocketServerInitializer.broadcast(message);
    }

    /**
     * Runs the webSocket server
     */
    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(webSocketServerInitializer);

            Channel ch = b.bind(port).sync().channel();

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Problems with webSocket connection", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
