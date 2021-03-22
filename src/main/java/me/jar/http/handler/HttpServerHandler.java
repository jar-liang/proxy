package me.jar.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @Description
 * @Date 2021/3/22-23:39
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) {
        System.out.println("收到客户端【" + ctx.channel().remoteAddress() + "】的请求...");
        System.out.println("请求uri=" + msg.uri());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
