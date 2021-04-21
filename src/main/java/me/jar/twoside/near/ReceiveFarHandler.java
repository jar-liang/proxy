package me.jar.twoside.near;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.jar.twoside.utils.TwoSideUtil;

/**
 * @Description
 * @Date 2021/4/21-22:23
 */
public class ReceiveFarHandler extends ChannelInboundHandlerAdapter {
    private final Channel clientChannel;

    public ReceiveFarHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (clientChannel.isActive()) {
            clientChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println("<<<数据已返回客户端");
                }
            });
        } else {
            System.out.println("客户端已断开...");
            TwoSideUtil.closeOnFlush(ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Far端连接已断开...");
        TwoSideUtil.closeOnFlush(clientChannel);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        TwoSideUtil.closeOnFlush(clientChannel);
        ctx.close();
    }
}
