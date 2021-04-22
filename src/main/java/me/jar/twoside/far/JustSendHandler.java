package me.jar.twoside.far;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.jar.twoside.utils.TwoSideUtil;

/**
 * @Description
 * @Date 2021/4/22-21:29
 */
public class JustSendHandler extends ChannelInboundHandlerAdapter {
    private final Channel remoteChannel;

    public JustSendHandler(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (remoteChannel.isActive()) {
            remoteChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> System.out.println(">>>Https数据已转发给remote端"));
        } else {
            System.out.println("remote端已断开...");
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("near端已断开连接...");
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
