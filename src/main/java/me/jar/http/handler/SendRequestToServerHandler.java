package me.jar.http.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

import java.util.Iterator;
import java.util.Map;

/**
 * @Description
 * @Date 2021/3/26-0:25
 */
public class SendRequestToServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    private Channel clientChannel;

    public SendRequestToServerHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        clientChannel.write(msg);
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;
            System.out.println("response http版本：" + httpResponse.protocolVersion().text());
            System.out.println("响应行：" + httpResponse.status().toString());
            HttpHeaders headers = httpResponse.headers();
            Iterator<Map.Entry<String, String>> entryIterator = headers.iteratorAsString();
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> next = entryIterator.next();
                System.out.println("响应头：" + next.getKey() + " = " + next.getValue());
            }
            ByteBuf byteBuf = Unpooled.copiedBuffer("\r\n\r\n", CharsetUtil.UTF_8);
            clientChannel.write(byteBuf);
        } else if (msg instanceof LastHttpContent) {
            clientChannel.flush();
            HttpContent httpContent = (HttpContent) msg;
            String content = httpContent.toString();
            System.out.println("最后响应内容->" + content);
        } else {
            System.out.println("其他内容->" + msg.toString());
        }
    }

}
