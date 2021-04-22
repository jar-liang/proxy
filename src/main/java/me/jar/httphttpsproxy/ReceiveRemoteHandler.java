package me.jar.httphttpsproxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Description
 * @Date 2021/4/23-0:00
 */
public class ReceiveRemoteHandler extends ChannelInboundHandlerAdapter {
    private final Channel clientChannel;

    public ReceiveRemoteHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (clientChannel.isActive()) {
            clientChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println("<<<回复响应给客户端");
                }
            });
        } else {
            System.out.println("客户端连接已断开...");
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("remote端断开...");
        ConnectRemoteHandler.closeOnFlush(clientChannel);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ConnectRemoteHandler.closeOnFlush(clientChannel);
        ctx.close();
    }
}
