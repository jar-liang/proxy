package me.jar.proxyhttpgetv2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;

/**
 * @Description 消息发送处理器
 *
 * @Date 2021/4/9-23:39
 */
public class SendHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Channel clientChannel = ctx.channel();
        // 目前只处理Http请求的GET方法，其他方法暂不处理
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            if (!HttpMethod.GET.equals(fullHttpRequest.method())) {
                System.out.println("不是GET请求...");
                return;
            }
            // 根据请求头的Host属性获取 host和port
            int port = 80;
            String host = fullHttpRequest.headers().get(HttpHeaderNames.HOST);
            if (host != null && host.contains(":")) {
                String[] split = host.split(":");
                host = split[0];
                port = Integer.parseInt(split[1]);
            }
            System.out.println("full http request->" + fullHttpRequest.uri());
            System.out.println("host->" + host + ", port->" + port);
            final String connectHost = host;
            final int connectPort = port;
            // 将连接远端服务器、转发请求、转发响应放到该Channel所在的eventLoop中执行，提高响应速度
            clientChannel.eventLoop().execute(() -> sendToRemote(fullHttpRequest, clientChannel, connectHost, connectPort));
        } else {
            System.out.println("other msg");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     * 翻译：在刷新所有排队的写入请求后关闭指定的通道
     * 这一方法挺重要的，可以给对应通道写入0长度的消息，然后关闭通道
     *
     * @param channel 通道
     */
    public static void closeOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void sendToRemote(FullHttpRequest httpRequest, Channel clientChannel, String host, int port) {
        Bootstrap bootstrap = new Bootstrap();
        // EventLoopGroup使用传入通道对应的，如果在这里new一个EventLoopGroup，会导致响应速度极其慢（坑！）
        bootstrap.group(clientChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 将http请求编码
                        pipeline.addLast("encoder", new HttpRequestEncoder());
                        // 接收远端服务器返回的消息，并返回给客户端（目前不需要其他处理，因此不用加response相关的解编码器）
                        pipeline.addLast("receiveHandler", new ReceiveHandler(clientChannel));
                    }
                });
        // 连接不用加sync方法，会出现异常
        ChannelFuture cf = bootstrap.connect(host, port);
        // 成功连接远端服务器后，发送请求消息（通过添加监听器执行）
        cf.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                future.channel().writeAndFlush(httpRequest).addListener((ChannelFutureListener) writeFuture -> {
                    if (writeFuture.isSuccess()) {
                        System.out.println("成功发送请求到远端服务器>>>");
                    } else {
                        System.out.println("发送请求到远端服务器失败...");
                        closeOnFlush(clientChannel);
                    }
                });
            } else {
                System.out.println("连接远端服务器失败...");
                closeOnFlush(clientChannel);
            }
        });
    }
}
