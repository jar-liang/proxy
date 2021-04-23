package me.jar.twoside.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.jar.twoside.constant.ProxyConstants;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

/**
 * @Description
 * @Date 2021/4/23-19:59
 */
public class EncryptHandler extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
        byte[] sourceBytes = new byte[msg.readableBytes()];
        msg.readBytes(sourceBytes);
        try {
            byte[] encrypt = AESUtil.encrypt(sourceBytes, ProxyConstants.PASSWORD);
            ByteBuf wrappedBuffer = Unpooled.wrappedBuffer(encrypt, ProxyConstants.DELIMITER);
            out.writeBytes(wrappedBuffer);
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
