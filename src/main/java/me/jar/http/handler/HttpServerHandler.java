package me.jar.http.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Date 2021/3/22-23:39
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final Map<Channel, BlockingQueue<HttpObject>> clientHttpObjectMap = new ConcurrentHashMap<>(10);

//    会将请求行和请求头整合起来形成一个请求DefaultHttpRequest传递到后面，把请求体再封装成消息体传递到后面，因为请求体可能很大，所以也可能会有多次封装，
//    那后面处理器就可能收到多次消息体。如果是GET的话是没有消息体的，首先收到一个DefaultHttpRequest，然后是一个空的LastHttpContent。如果是POST的话，
//    先收到DefaultHttpRequest，然后可能多个内容DefaultHttpContent和一个DefaultLastHttpContent
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        Channel channel = ctx.channel();
        if (msg instanceof HttpRequest) {
            System.out.println("HttpRequest 类型 msg，channel->" + channel.hashCode());
            HttpRequest httpRequest = (HttpRequest) msg;
            if (HttpMethod.GET.equals(httpRequest.method())) {
                BlockingQueue<HttpObject> httpObjectQueue = new ArrayBlockingQueue<>(100);
                boolean offer = httpObjectQueue.offer(msg);
                if (offer) {
                    clientHttpObjectMap.put(channel, httpObjectQueue);
                }
            }
        } else if (msg instanceof DefaultHttpContent) {
            System.out.println("DefaultHttpContent 类型 msg，channel->" + channel.hashCode());
            BlockingQueue<HttpObject> httpObjects = clientHttpObjectMap.get(channel);
            if (httpObjects != null) {
                httpObjects.offer(msg);
            }
        } else if (msg instanceof LastHttpContent) {
            System.out.println("LastHttpContent 类型 msg，channel->" + channel.hashCode());
            BlockingQueue<HttpObject> httpObjects = clientHttpObjectMap.get(channel);
            if (httpObjects != null && httpObjects.offer(msg)) {
                System.out.println("开始向目标服务器请求数据>>>");
                new TransferGetRequest(httpObjects, channel).run();
            }
        } else {
            System.out.println("HttpRequest 其他类型，channel->" + channel.hashCode());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
