package me.jar.proxyhttpgetv2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Description 响应消息处理器，将远端服务器响应数据转发回客户端
 *
 * @Date 2021/4/10-0:09
 */
public class ReceiveHandler extends ChannelInboundHandlerAdapter {
    /**
     * 客户端连接的通道，创建响应消息处理器的时候传入
     */
    private final Channel clientChannel;

    public ReceiveHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 直接转发回客户端即可
        clientChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println("回复响应给客户端>>>");
            } else {
                future.channel().close();
            }
        });
    }

    /**
     * 当服务器完成响应后会将连接断开，触发该方法。此时响应数据已返回完成，将客户端连接通道关闭，完成响应的转发
     *
     * @param ctx 通道处理器上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("remote channel 关闭...");
        SendHandler.closeOnFlush(clientChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        SendHandler.closeOnFlush(ctx.channel());
    }
}
