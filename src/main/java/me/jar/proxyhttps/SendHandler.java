package me.jar.proxyhttps;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Description
 * @Date 2021/4/18-22:48
 */
public class SendHandler extends ChannelInboundHandlerAdapter {
    private final Channel remoteChannel;

    public SendHandler(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        remoteChannel.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        HttpsConnectHandler.closeOnFlush(ctx.channel());
        HttpsConnectHandler.closeOnFlush(remoteChannel);
    }
}
