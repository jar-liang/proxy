package me.jar.httphttpsproxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.util.CharsetUtil;

/**
 * @Description
 * @Date 2021/4/22-23:51
 */
public class ConnectRemoteHandler extends ChannelInboundHandlerAdapter {
    private Channel remoteChannel;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest httpRequest = (FullHttpRequest) msg;
            System.out.println(">>>收到http请求，uri->" + httpRequest.uri());
            if (HttpMethod.CONNECT.equals(httpRequest.method())) {
                connectRemoteReplyClient(ctx, httpRequest);
            } else {
                sendRemote(ctx, httpRequest);
            }
        } else {
            System.out.println(">>>收到其他类型的数据->" + msg.getClass());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("客户端channel已断开...");
        closeOnFlush(remoteChannel);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(remoteChannel);
        ctx.close();
    }

    private void connectRemoteReplyClient(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        String host = httpRequest.uri();
        int port = 443;
        if (host != null && host.contains(":")) {
            String[] split = host.split(":");
            if (split.length == 2) {
                host = split[0];
                try {
                    port = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    System.out.println("转换数字异常: " + e.getMessage());
                }
            }
        }
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop()).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("receive", new ReceiveRemoteHandler(ctx.channel()));
                    }
                });
        bootstrap.connect(host, port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println(">>>连接远端服务器完成(https)");
                Channel remoteChannel = future.channel();
                ChannelPipeline clientPipeline = ctx.channel().pipeline();
                ByteBuf response = Unpooled.copiedBuffer("HTTP/1.1 200 Connection Established\r\n\r\n", CharsetUtil.ISO_8859_1);
                ctx.writeAndFlush(response).addListener((ChannelFutureListener) replyFuture -> {
                    if (replyFuture.isSuccess()) {
                        System.out.println("<<<已回复客户端隧道已建立");
                        clientPipeline.remove("decoder");
                        clientPipeline.remove("aggregator");
                        clientPipeline.remove("connectHandler");
                        clientPipeline.addLast("sendData", new SendDataHandler(remoteChannel));
                    }
                });
            }
        });
    }

    private void sendRemote(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        if (remoteChannel != null && remoteChannel.isActive()) {
            remoteChannel.writeAndFlush(httpRequest).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println(">>>发送请求给远端服务器完成（remoteChannel已创建）");
                }
            });
        } else {
            String host = httpRequest.headers().get(HttpHeaderNames.HOST);
            int port = 80;
            if (host != null && host.contains(":")) {
                String[] split = host.split(":");
                if (split.length == 2) {
                    host = split[0];
                    try {
                        port = Integer.parseInt(split[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("转换数字异常: " + e.getMessage());
                    }
                }
            }

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop()).channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("encoder", new HttpRequestEncoder());
                            pipeline.addLast("receive", new ReceiveRemoteHandler(ctx.channel()));
                        }
                    });
            bootstrap.connect(host, port).addListener((ChannelFutureListener) connectFuture -> {
                if (connectFuture.isSuccess()) {
                    System.out.println(">>>连接远端服务器完成(http)");
                    remoteChannel = connectFuture.channel();
                    remoteChannel.writeAndFlush(httpRequest).addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            System.out.println(">>>发送请求给远端服务器完成（刚创建remoteChannel）");
                        }
                    });
                }
            });
        }
    }

    public static void closeOnFlush(Channel channel) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
