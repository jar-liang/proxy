package me.jar.twoside.far;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.jar.twoside.utils.TwoSideUtil;

/**
 * @Description
 * @Date 2021/4/21-22:48
 */
public class ReceiveRemoteHandler extends ChannelInboundHandlerAdapter {
    private final Channel nearChannel;

    public ReceiveRemoteHandler(Channel nearChannel) {
        this.nearChannel = nearChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (nearChannel.isActive()) {
            nearChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println("<<<回复响应给near端");
                }
            });
        } else {
            System.out.println("near端连接已断开...");
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("remote端断开...");
        TwoSideUtil.closeOnFlush(nearChannel);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        TwoSideUtil.closeOnFlush(nearChannel);
        ctx.close();
    }
}
