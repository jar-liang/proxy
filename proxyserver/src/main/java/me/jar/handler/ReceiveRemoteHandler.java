package me.jar.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.jar.utils.NettyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 * @Date 2021/4/26-0:07
 */
public class ReceiveRemoteHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveRemoteHandler.class);

    private final Channel nearChannel;

    public ReceiveRemoteHandler(Channel nearChannel) {
        this.nearChannel = nearChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (nearChannel.isActive()) {
            nearChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    LOGGER.debug("<<<Has been replying response data to client");
                }
            });
        } else {
            LOGGER.info("===Client channel disconnected, no transferring data.");
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.info("===Remote channel disconnected");
        NettyUtil.closeOnFlush(nearChannel);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("===ReceiveRemoteHandler caught exception", cause);
        NettyUtil.closeOnFlush(nearChannel);
        ctx.close();
    }
}
