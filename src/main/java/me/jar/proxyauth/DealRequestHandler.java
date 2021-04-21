package me.jar.proxyauth;

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
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * @Description
 * @Date 2021/4/19-21:49
 */
public class DealRequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest httpRequest = (FullHttpRequest) msg;
            System.out.println(">>>收到请求->" +httpRequest.uri());
            if (HttpMethod.CONNECT.equals(httpRequest.method())) {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
                response.headers().add(HttpHeaderNames.CONTENT_LENGTH, 0);
                ctx.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
                    System.out.println("暂不处理https，回复响应给客户端<<<");
                    ctx.close();
                });
                return;
            }

            Iterator<Map.Entry<String, String>> entryIterator = httpRequest.headers().iteratorAsString();
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> next = entryIterator.next();
                System.out.println("处理前的http请求头->" + next.getKey() + ": " + next.getValue());
            }
            // 如果不含Proxy-Authorization 或 摘要认证不通过，返回需要认证的响应
            boolean hasAuth = httpRequest.headers().contains(HttpHeaderNames.PROXY_AUTHORIZATION);
            if (!hasAuth) {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
                String nonce = UUID.randomUUID().toString().replace("-", "");
                String opaque = UUID.randomUUID().toString().replace("-", "");
                StringBuilder builder = new StringBuilder(256);
                builder.append("Digest realm=\"localhost\" qop=\"auth\" nonce=\"").append(nonce).append("\" opaque=\"").append(opaque).append("\"");
                response.headers().add("Proxy-Authenticate", builder.toString()).add(HttpHeaderNames.CONTENT_LENGTH, 0);
                ctx.writeAndFlush(response).addListener((ChannelFutureListener) future -> System.out.println("发送请求认证响应成功<<<"));
                return;
            }

            httpRequest.headers().remove(HttpHeaderNames.PROXY_AUTHORIZATION);

            Iterator<Map.Entry<String, String>> entryIteratorLater = httpRequest.headers().iteratorAsString();
            while (entryIteratorLater.hasNext()) {
                Map.Entry<String, String> next = entryIteratorLater.next();
                System.out.println("处理后的http请求头->" + next.getKey() + ": " + next.getValue());
            }

            String host = httpRequest.headers().get(HttpHeaderNames.HOST);
            int port = 80;
            if (host.contains(":")) {
                String[] split = host.split(":");
                if (split.length == 2) {
                    host = split[0];
                    port = Integer.parseInt(split[1]);
                }
            }

            final String remoteHost = host;
            final int remotePort = port;
            ctx.channel().eventLoop().execute(() -> connectRemote(ctx.channel(), httpRequest, remoteHost, remotePort));
        }
    }

    private void connectRemote(Channel clientChannel, HttpRequest httpRequest, String host, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientChannel.eventLoop()).channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("encoder", new HttpRequestEncoder());
                    pipeline.addLast("receiveHandler", new ReceiveRemoteHandler(clientChannel));
                }
            });
        bootstrap.connect(host, port).addListener((ChannelFutureListener) future -> {
            System.out.println(">>>连接到目标服务器成功");
            Channel remoteChannel = future.channel();
            ChannelPipeline clientPipeline = clientChannel.pipeline();
            clientPipeline.remove("codec");
            clientPipeline.remove("aggregator");
            remoteChannel.writeAndFlush(httpRequest).addListener((ChannelFutureListener) future1 -> System.out.println("请求已发送到远端客户端>>>"));
        });
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
}
