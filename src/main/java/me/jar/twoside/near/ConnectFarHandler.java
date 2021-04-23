package me.jar.twoside.near;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import me.jar.twoside.constant.ProxyConstants;
import me.jar.twoside.utils.DecryptHandler;
import me.jar.twoside.utils.EncryptHandler;
import me.jar.twoside.utils.TwoSideUtil;

/**
 * @Description
 * @Date 2021/4/21-21:31
 */
public class ConnectFarHandler extends ChannelInboundHandlerAdapter {
    private Channel farChannel;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 直连far端，将数据发送过去
        if (farChannel != null && farChannel.isActive()) {
            farChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println(">>>数据已发送到Far端（farChannel 已创建）");
                }
            });
        } else {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop()).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("delimiter", new DelimiterBasedFrameDecoder(ProxyConstants.MAX_FRAME_LENGTH, Unpooled.wrappedBuffer(ProxyConstants.DELIMITER)));
                        pipeline.addLast("decrypt", new DecryptHandler());
                        pipeline.addLast("encrypt", new EncryptHandler());
                        pipeline.addLast("receiveFar", new ReceiveFarHandler(ctx.channel()));
                    }
                });
            bootstrap.connect(ProxyConstants.FAR_SERVER_HOST, ProxyConstants.FAR_SERVER_PORT).addListener((ChannelFutureListener) connectFuture -> {
                if (connectFuture.isSuccess()) {
                    System.out.println(">>>已成功连接far服务端");
                    farChannel = connectFuture.channel();
                    farChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            System.out.println(">>>数据已发送到Far端（刚创建farChannel）");
                        }
                    });
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("客户端连接已断开...");
        TwoSideUtil.closeOnFlush(farChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        TwoSideUtil.closeOnFlush(farChannel);
        ctx.close();
    }
}
