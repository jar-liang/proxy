package me.jar.proxyhttps;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @Description
 * @Date 2021/4/18-22:28
 */
public class HttpsConnectHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            String uri = fullHttpRequest.uri();
            System.out.println("===收到http请求->" + uri);
            if (HttpMethod.CONNECT.equals(fullHttpRequest.method())) {
                // 取出host和port，连接远端服务器，回复客户端连接已建立，后续提供通信隧道
                String host = uri;
                int port = 443;
                if (uri.contains(":")) {
                    String[] split = uri.split(":");
                    if (split.length == 2) {
                        host = split[0];
                        port = Integer.parseInt(split[1]);
                    }
                }
                connectRemote(ctx.channel(), host, port);
            } else {
                System.out.println("不是https方式，请求方法->" + fullHttpRequest.method().asciiName());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    public static void closeOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void connectRemote(Channel clientChannel, String host, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // 仅仅将远端返回数据给回客户端
                        ch.pipeline().addLast(new ReceiveHandler(clientChannel));
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect(host, port);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                System.out.println("===连接远端服务器成功>>>");
                Channel remoteChannel = future.channel();
                // 连接远端客户端成功后，回复连接已建立给客户端，然后移除管道中原有的Handler，添加新的发送数据Handler（单单转发数据）
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(200, "Connection Established"));
                clientChannel.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        ChannelPipeline clientPipeline = clientChannel.pipeline();
                        clientPipeline.remove("codec");
                        clientPipeline.remove("aggregator");
                        clientPipeline.remove("connectHandler");
                        // 仅仅将客户端数据发给远端服务器
                        clientPipeline.addLast("sender", new SendHandler(remoteChannel));
                    }
                });
            }
        });
    }
}
