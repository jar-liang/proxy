package me.jar.proxyhttpgetv2;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * @Description 管道初始化处理器
 *
 * @Date 2021/4/9-23:33
 */
public class ProxyServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        // 添加http请求的解码器，方便后面的发送消息处理器读取请求host和port
        pipeline.addLast("decoder", new HttpRequestDecoder());
        // 添加http消息聚合处理器，可以得到完整的http请求消息，方便直接发送到远端服务器，大小定义20MB（根据情况来，目前不确定放多大比较合适）
        pipeline.addLast("aggregator", new HttpObjectAggregator(20 * 1024 * 1024));
        // 添加http响应的编码器
        pipeline.addLast("encoder", new HttpResponseEncoder());
        // 消息发送处理器，将客户端发送过来的http请求发送给远端服务器，然后启动消息接收处理将响应数据返回客户端
        pipeline.addLast("sendHandler", new SendHandler());
    }
}
