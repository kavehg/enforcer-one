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

import com.google.gson.Gson;
import io.enforcer.deathstar.pojos.MetricRequest;
import io.enforcer.vader.Vader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Handles handshakes and messages - Modified version of the
 * webSocket handler from netty examples
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * Class logger
     */
    private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class.getName());

    /**
     * Path on which webSocket requests will be handled
     */
    private static final String WEBSOCKET_PATH = "/websocket";

    /**
     * Conducts the webSocket handshake
     */
    private WebSocketServerHandshaker handshaker;


    private Map<String, Vader> vaderMap = new ConcurrentHashMap<>(1000);
    /**
     * Handler for webSockets
     */
    public WebSocketServerHandler() {
        super();
        logger.log(Level.FINE, "WebSocketServerHandler instantiated: {0}", this);
    }

    /**
     * Invoked each time a request is received
     *
     * @param ctx handler context
     * @param msg message received
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * Invoked when the read is complete
     *
     * @param ctx handler context
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * Handles and incoming request and checks if its an http request
     * or if we can initiate the webSocket protocol handshake
     *
     * @param ctx handler context
     * @param req http request
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.method() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
            logger.log(Level.FINE, "web socket client connected: " + ctx.channel());
        }
    }

    /**
     * Handle incoming Vader requests with different operations for ADD, EDIT, and REMOVE
     * @param ctx
     * @param frame
     * @return
     */
    private boolean handleVaderRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (((TextWebSocketFrame) frame).text().startsWith("{\"url\"")) {
            String json = ((TextWebSocketFrame) frame).text();
            Gson gson = new Gson();
            MetricRequest req = gson.fromJson(json, MetricRequest.class);
            if (checkMetricValidity(req.url)) {
                if (req.type.equals("ADD")) {
                    Vader newVader = new Vader(req);
                    vaderMap.put(req.metricDetail, newVader);
                }
                else if (req.type.equals("EDIT")) {
                    Vader vader = vaderMap.remove(req.metricDetail);
                    vader.getGraphiteMaster().stopMetricMonitoring();
                    vader = null;
                    Vader newVader = new Vader(req);
                    vaderMap.put(req.metricDetail, newVader);
                }
                else if (req.type.equals("REMOVE")) {
                    Vader removeVader = vaderMap.remove(req.metricDetail);
                    removeVader.getGraphiteMaster().stopMetricMonitoring();
                    removeVader = null;
                    logger.log(Level.INFO, req.metricDetail + " Removed.");
                }
                logger.log(Level.INFO, String.valueOf(vaderMap.size()));
            }
            else {
                if (req.type.equals("ADD")) {
                    ctx.channel().write(new TextWebSocketFrame("ERROR: Could not process Metric(s) request"));
                    req.type = "REMOVE";
                    ctx.channel().write(new TextWebSocketFrame(gson.toJson(req)));
                }
                else if (req.type.equals("EDIT")){
                    ctx.channel().write(new TextWebSocketFrame("ERROR: Vader could not be edited."));
                }
                else if (req.type.equals("REMOVE")) {
                    Vader removeVader = vaderMap.remove(req.metricDetail);
                    removeVader.getGraphiteMaster().stopMetricMonitoring();
                    removeVader = null;
                    logger.log(Level.INFO, req.metricDetail + " Removed.");
                }
            }
            return true;
        }
        return false;

    }


    /**
     * Checks validity of metric path. If an empty json object is returned
     * the client will be informed that the Vader could not be created.
     * @param url
     * @return
     */
    private boolean checkMetricValidity(String url) {
        try {
            URL http = new URL(url);

            BufferedReader reader = new BufferedReader(new InputStreamReader(http.openStream()));
            StringBuilder invalid = new StringBuilder();
            for (int i = 0; i < 2; i++) {
                char c = (char) reader.read();
                invalid.append(c);
            }

            if (invalid.toString().equals("[]")){
                logger.log(Level.INFO, "Metric Request Invalid");
                return false;
            }
            return true;
        }
        catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Metric Request Invalid");
            return false;
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Could not retrieve Metric.");
            return false;
        }
    }

    /**
     * Invoked once we determine that the http request is using the webSocket
     * protocol
     *
     * @param ctx handler context
     * @param frame webSocket frame
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            logger.log(Level.FINE, "web socket client disconnected: " + ctx.channel());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }

        if (!handleVaderRequest(ctx, frame)) {
            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            System.err.printf("%s received %s%n", ctx.channel(), request);
            ctx.channel().write(new TextWebSocketFrame(request.toUpperCase()));
        }
    }

    /**
     * Sends http response to clients
     *
     * @param ctx handler context
     * @param req http request
     * @param res http response
     */
    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaderUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaderUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Invoked in case any exceptions in processing the http request
     * @param ctx handler context
     * @param cause cause of the exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * Constructs the webSocket URL
     *
     * @param req http request
     * @return webSocket URL
     */
    private static String getWebSocketLocation(FullHttpRequest req) {
        String location =  req.headers().get(HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }
}
