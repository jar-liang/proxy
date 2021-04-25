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
 * @Date 2021/4/26-0:13
 */
public class JustSendHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JustSendHandler.class);

    private final Channel remoteChannel;

    public JustSendHandler(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (remoteChannel.isActive()) {
            remoteChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    LOGGER.debug("<<<Has been replying request data (https) to remote");
                }
            });
        } else {
            LOGGER.info("===Remote channel disconnected, no transferring data.");
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.info("===Client channel disconnected");
        NettyUtil.closeOnFlush(remoteChannel);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("===JustSendHandler caught exception", cause);
        NettyUtil.closeOnFlush(remoteChannel);
        ctx.close();
    }
}
