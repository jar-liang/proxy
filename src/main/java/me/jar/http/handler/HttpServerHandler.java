package me.jar.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

/**
 * @Description
 * @Date 2021/3/22-23:39
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) {
        System.out.println("收到客户端【" + ctx.channel().remoteAddress() + "】的请求...");
        System.out.println("请求uri=" + msg.uri());
        // 发送http请求到服务器，再将响应内容回复给客户端
        // 这块有问题
        HttpGet httpGet = new HttpGet(msg.uri());
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build();
             CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            ctx.writeAndFlush(httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
