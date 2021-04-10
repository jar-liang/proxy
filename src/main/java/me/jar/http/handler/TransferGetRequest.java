package me.jar.http.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.concurrent.BlockingQueue;

/**
 * @Description
 * @Date 2021/3/26-0:11
 */
public class TransferGetRequest {
    private BlockingQueue<HttpObject> httpObjects;
    private Channel clientChannel;

    public TransferGetRequest(BlockingQueue<HttpObject> httpObjects, Channel clientChannel) {
        this.httpObjects = httpObjects;
        this.clientChannel = clientChannel;
    }

    public void run() {
        // 向客户端请求的服务器发送请求
        EventLoopGroup loopGroup = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(loopGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 发送请求到服务器，得到返回的请求信息再发回给客户端
                            pipeline.addLast(new HttpRequestEncoder());
                            pipeline.addLast(new HttpResponseDecoder());
                            pipeline.addLast(new SendRequestToServerHandler(clientChannel));
                        }
                    });
            HttpObject httpObject = httpObjects.poll();
            if (httpObject instanceof HttpRequest) {
                HttpRequest httpRequest = (HttpRequest) httpObject;
                String host = httpRequest.headers().get("Host");
                System.out.println("请求的host=" + host);
                ChannelFuture cf = bootstrap.connect(host, 80).sync();
                Channel channel = cf.channel();
                channel.write(httpRequest);

                while (!httpObjects.isEmpty()) {
                    HttpObject object = httpObjects.poll();
                    channel.write(object);
                    if (object instanceof LastHttpContent) {
                        break;
                    }
                }
                channel.flush();
                channel.closeFuture().sync();
                System.out.println("请求服务器结束");
                Thread.sleep(10 * 1000L);
                clientChannel.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            loopGroup.shutdownGracefully();
        }
    }
}