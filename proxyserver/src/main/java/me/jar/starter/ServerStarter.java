package me.jar.starter;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import me.jar.constants.ProxyConstants;
import me.jar.utils.NettyUtil;
import me.jar.utils.PlatformUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @Description
 * @Date 2021/4/23-23:45
 */
public class ServerStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStarter.class);

    private final int port;

    public ServerStarter(int port) {
        this.port = port;
    }

    public void run() {
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                // 添加与客户端交互的handler
            }
        };
        NettyUtil.starServer(port, channelInitializer);
    }

    public static void main(String[] args) {
        if (ProxyConstants.PROPERTY.containsKey(ProxyConstants.KEY_NAME_PORT)) {
            String port = ProxyConstants.PROPERTY.get(ProxyConstants.KEY_NAME_PORT);
            try {
                int portNum = Integer.parseInt(port.trim());
                new ServerStarter(portNum).run();
            } catch (NumberFormatException e) {
                LOGGER.error("===Failed to parse number, property setting may be wrong.", e);
            }
        }
    }
}
