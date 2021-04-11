package me.jar.proxyhttp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.Iterator;
import java.util.Map;

/**
 * @Description 响应消息处理器，将远端服务器响应数据转发回客户端
 *
 * @Date 2021/4/10-0:09
 */
public class ReceiveHandler extends ChannelInboundHandlerAdapter {
    /**
     * 客户端连接的通道，创建响应消息处理器的时候传入
     */
    private final Channel clientChannel;

    public ReceiveHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;
            System.out.println("接收到http response->" + httpResponse.status().codeAsText() + " . class->" + httpResponse.getClass());
            System.out.println("****************************响应头开始***********************************");
            Iterator<Map.Entry<String, String>> entryIterator = httpResponse.headers().iteratorAsString();
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> next = entryIterator.next();
                System.out.println(next.getKey() + ": " + next.getValue());
            }
            System.out.println("****************************响应头结束***********************************");
            replyToClient(httpResponse);
        } else if (msg instanceof DefaultHttpContent) {
            DefaultHttpContent httpContent = (DefaultHttpContent) msg;
            System.out.println("接收到http content->" + httpContent.content().readableBytes() + " byte . class->" + httpContent.getClass());
            replyToClient(httpContent);
        } else if (msg instanceof LastHttpContent) {
            LastHttpContent lastHttpContent = (LastHttpContent) msg;
            System.out.println("接收到last http content->" + lastHttpContent.content().readableBytes() + " byte . class->" + lastHttpContent.getClass());
            replyToClient(lastHttpContent);
        } else {
            System.out.println("其他类型数据->" + msg.getClass());
        }
    }

    /**
     * 当服务器将连接断开，触发该方法。将客户端连接通道关闭
     *
     * @param ctx 通道处理器上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("remote channel 关闭...");
        SendHandler.closeOnFlush(clientChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        SendHandler.closeOnFlush(ctx.channel());
    }

    private void replyToClient(HttpObject msg) {
            clientChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println("回复响应给客户端>>>");
            } else {
                future.channel().close();
            }
        });
    }
}
