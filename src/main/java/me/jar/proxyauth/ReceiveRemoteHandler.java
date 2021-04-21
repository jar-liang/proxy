package me.jar.proxyauth;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Description
 * @Date 2021/4/19-22:02
 */
public class ReceiveRemoteHandler extends ChannelInboundHandlerAdapter {
    private final Channel clientChannel;

    public ReceiveRemoteHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        clientChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> System.out.println("将数据返回给客户端了<<<"));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("远端服务器断开连接<<<");
        DealRequestHandler.closeOnFlush(clientChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        DealRequestHandler.closeOnFlush(clientChannel);
    }
}
