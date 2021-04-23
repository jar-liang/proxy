package me.jar.twoside.constant;

/**
 * @Description
 * @Date 2021/4/21-22:12
 */
public interface ProxyConstants {
    String FAR_SERVER_HOST = "127.0.0.1";

    int FAR_SERVER_PORT = 9090;

    String PASSWORD = "0123456789abcdef0123456789abcdef";

    byte[] DELIMITER = new byte[] {13, 13, 13, 13, 10, 10, 10, 10};

    int MAX_FRAME_LENGTH = 128 * 1024;
}
