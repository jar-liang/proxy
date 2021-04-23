package me.jar.twoside.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.jar.twoside.constant.ProxyConstants;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * @Description
 * @Date 2021/4/23-20:07
 */
public class DecryptHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        byte[] encryptSource = new byte[in.readableBytes()];
        in.readBytes(encryptSource);
        try {
            byte[] decryptBytes = AESUtil.decrypt(encryptSource, ProxyConstants.PASSWORD);
            out.add(Unpooled.wrappedBuffer(decryptBytes));
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
