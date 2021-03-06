package me.jar.twoside.far;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import me.jar.twoside.constant.ProxyConstants;
import me.jar.twoside.utils.DecryptHandler;
import me.jar.twoside.utils.EncryptHandler;
import me.jar.twoside.utils.TwoSideUtil;

/**
 * @Description
 * @Date 2021/4/21-21:27
 */
public class FarServer {
    private final int port;

    public FarServer(int port) {
        this.port = port;
    }

    public void run() {
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("delimiter", new DelimiterBasedFrameDecoder(ProxyConstants.MAX_FRAME_LENGTH, Unpooled.wrappedBuffer(ProxyConstants.DELIMITER)));
                pipeline.addLast("decrypt", new DecryptHandler());
                pipeline.addLast("decoder", new HttpRequestDecoder());
                pipeline.addLast("aggregator", new HttpObjectAggregator(20 * 1024 * 1024));
                pipeline.addLast("encrypt", new EncryptHandler());
                pipeline.addLast("connectRemote", new ConnectRemoteHandler());
            }
        };
        TwoSideUtil.starServer(port, channelInitializer);
    }

    public static void main(String[] args) {
        new FarServer(9090).run();
    }
}
