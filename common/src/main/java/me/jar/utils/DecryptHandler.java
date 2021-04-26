package me.jar.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.jar.constants.ProxyConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * @Description
 * @Date 2021/4/23-20:07
 */
public class DecryptHandler extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DecryptHandler.class);

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
        // 先判断是否有特定标识字节，没有则直接关闭通道
        byte[] markBytes = new byte[4];
        int readableBytes = in.readableBytes();
        in.getBytes(readableBytes - 4, markBytes);
        for (int i = 0; i < markBytes.length; i++) {
            if (markBytes[i] != 8) {
                LOGGER.info("===Illegal data from ip: {}", ctx.channel().remoteAddress());
                in.readerIndex(in.writerIndex());
                ctx.close();
                return;
            }
        }
        byte[] encryptSource = new byte[readableBytes - 4];
        in.readBytes(encryptSource, 0, readableBytes - 4);
        in.readerIndex(in.writerIndex());
        try {
            byte[] decryptBytes = AESUtil.decrypt(encryptSource, password);
            out.add(Unpooled.wrappedBuffer(decryptBytes));
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            LOGGER.error("===Decrypt data failed. detail: {}", e.getMessage());
            ctx.close();
        }
    }
}
