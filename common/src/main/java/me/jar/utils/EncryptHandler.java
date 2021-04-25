package me.jar.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.jar.constants.ProxyConstants;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

/**
 * @Description
 * @Date 2021/4/23-19:59
 */
public class EncryptHandler extends MessageToByteEncoder<ByteBuf> {
    private String password;

    public EncryptHandler() {
        String password = ProxyConstants.PROPERTY.get(ProxyConstants.PROPERTY_NAME_KEY);
        if (password == null || password.length() == 0) {
            throw new IllegalArgumentException("Illegal key from property");
        }
        this.password = password;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
        byte[] sourceBytes = new byte[msg.readableBytes()];
        msg.readBytes(sourceBytes);
        try {
            byte[] encrypt = AESUtil.encrypt(sourceBytes, password);
            ByteBuf wrappedBuffer = Unpooled.wrappedBuffer(encrypt, ProxyConstants.DELIMITER);
            out.writeBytes(wrappedBuffer);
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
