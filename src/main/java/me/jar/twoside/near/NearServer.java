package me.jar.twoside.near;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import me.jar.twoside.utils.TwoSideUtil;

/**
 * @Description
 * @Date 2021/4/21-21:27
 */
public class NearServer {
    private final int port;

    public NearServer(int port) {
        this.port = port;
    }

    public void run() {
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast("connectFar", new ConnectFarHandler());
            }
        };
        TwoSideUtil.starServer(port, channelInitializer);
    }

    public static void main(String[] args) {
        new NearServer(8080).run();
    }
}
