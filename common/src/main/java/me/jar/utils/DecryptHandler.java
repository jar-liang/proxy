package me.jar.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.jar.constants.ProxyConstants;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * @Description
 * @Date 2021/4/23-20:07
 */
public class DecryptHandler extends ByteToMessageDecoder {
    private String password;

    public DecryptHandler() {
        String password = ProxyConstants.PROPERTY.get(ProxyConstants.PROPERTY_NAME_KEY);
        if (password == null || password.length() == 0) {
            throw new IllegalArgumentException("Illegal key from property");
        }
        this.password = password;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        byte[] encryptSource = new byte[in.readableBytes()];
        in.readBytes(encryptSource);
        try {
            byte[] decryptBytes = AESUtil.decrypt(encryptSource, password);
            out.add(Unpooled.wrappedBuffer(decryptBytes));
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
