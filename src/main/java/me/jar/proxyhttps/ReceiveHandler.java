package me.jar.proxyhttps;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Description
 * @Date 2021/4/18-22:54
 */
public class ReceiveHandler extends ChannelInboundHandlerAdapter {
    private final Channel clientChannel;

    public ReceiveHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        clientChannel.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        HttpsConnectHandler.closeOnFlush(ctx.channel());
        HttpsConnectHandler.closeOnFlush(clientChannel);
    }
}
