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
 * From netty examples
 *
 * A HTTP server which serves Web Socket requests at:
 *
 * http://localhost:<PORT>/websocket
 */
public final class WebSocketServer implements Runnable {

    private static final Logger logger = Logger.getLogger(WebSocketServer.class.getName());
    static final boolean SSL = System.getProperty("ssl") != null;
    private WebSocketServerHandler webSocketServerHandler;
    private final Integer port;

    /**
     * Creates an instance of the web socket server on the specified port
     *
     * @param port used for web socket
     */
    public WebSocketServer(Integer port) {
        this.port = port;
    }

    /**
     * Publish a web socket frame to all web socket clients
     *
     * @param message to be broadcast
     */
    public void broadcastToAllClients(String message) {
        webSocketServerHandler.broadcast(message);
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        webSocketServerHandler = new WebSocketServerHandler();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new WebSocketServerInitializer(null, webSocketServerHandler));

            Channel ch = b.bind(port).sync().channel();

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Problems with websocket connection", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
