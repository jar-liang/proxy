package me.jar.twoside.utils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import me.jar.twoside.bean.HostAndPort;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

/**
 * @Description
 * @Date 2021/4/21-21:33
 */
public final class TwoSideUtil {
    private TwoSideUtil() {
    }



    public static void closeOnFlush(Channel channel) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static HostAndPort parseHostAndPort(String message, int defaultPort) {
        int port = defaultPort;
        if (message == null || message.length() == 0) {
            return new HostAndPort("", port);
        }
        String host = message;
        if (message.contains(":")) {
            String[] split = message.split(":");
            if (split.length == 2) {
                host = split[0];
                try {
                    port = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    System.out.println("转换数字异常: " + e.getMessage());
                }
            }
        }
        return new HostAndPort(host, port);
    }

    public static void starServer(int port, ChannelHandler chanelInitializer) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(chanelInitializer);
            ChannelFuture cf = serverBootstrap.bind(port).sync();
            System.out.println(">>>代理服务器已启动，端口：" + port);
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws GeneralSecurityException, UnsupportedEncodingException {
        String text = "hello world";
        String key = "0123456789abcdef0123456789abcdef";
        byte[] textBytes = text.getBytes(CharsetUtil.UTF_8);
        System.out.println("原文字节数组长度->" + textBytes.length);
        byte[] encrypt = AESUtil.encrypt(textBytes, key);
        System.out.println("加密字节数组长度->" + encrypt.length);
        byte[] decrypt = AESUtil.decrypt(encrypt, key);
        System.out.println("解密字节数组长度->" + decrypt.length);
        System.out.println("原文->" + new String(decrypt, CharsetUtil.UTF_8));
    }
}
