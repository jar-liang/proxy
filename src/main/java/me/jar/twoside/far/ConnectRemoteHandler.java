package me.jar.twoside.far;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import me.jar.twoside.bean.HostAndPort;
import me.jar.twoside.utils.TwoSideUtil;

/**
 * @Description
 * @Date 2021/4/21-21:59
 */
public class ConnectRemoteHandler extends ChannelInboundHandlerAdapter {
    private Channel remoteChannel;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Https后面再考虑 TODO
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest httpRequest = (FullHttpRequest) msg;
            if (HttpMethod.CONNECT.equals(httpRequest.method())) {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
                response.headers().add(HttpHeaderNames.CONTENT_LENGTH, 0);
                ctx.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        System.out.println("<<<已回复客户端不允许Https的请求");
                    }
                });
                return;
            }

            if (remoteChannel != null && remoteChannel.isActive()) {
                remoteChannel.writeAndFlush(httpRequest).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        System.out.println(">>>发送请求给远端服务器完成（remoteChannel已创建）");
                    }
                });
            } else {
                String address = httpRequest.headers().get(HttpHeaderNames.HOST);
                HostAndPort hostAndPort = TwoSideUtil.parseHostAndPort(address);

                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(ctx.channel().eventLoop()).channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("encoder", new HttpRequestEncoder());
                            pipeline.addLast("receiveRemote", new ReceiveRemoteHandler(ctx.channel()));
                        }
                    });
                bootstrap.connect(hostAndPort.getHost(), hostAndPort.getPort()).addListener((ChannelFutureListener) connectFuture -> {
                    if (connectFuture.isSuccess()) {
                        System.out.println(">>>连接远端服务器完成");
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
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("near端channel已断开...");
        TwoSideUtil.closeOnFlush(remoteChannel);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        TwoSideUtil.closeOnFlush(remoteChannel);
        ctx.close();
    }
}
