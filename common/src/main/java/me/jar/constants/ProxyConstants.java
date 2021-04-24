package me.jar.constants;

/**
 * @Description
 * @Date 2021/4/21-22:12
 */
public interface ProxyConstants {

    String PASSWORD = "0123456789abcdef0123456789abcdef";

    byte[] DELIMITER = new byte[] {13, 13, 13, 13, 10, 10, 10, 10};

    int MAX_FRAME_LENGTH = 128 * 1024;

    int WIN_OS = 1;

    int LINUX_OS = 2;

    int OTHER_OS = 3;

    String PROPERTY_NAME_WIN = "D:\\usr\\property\\property.txt";

    String PROPERTY_NAME_LINUX= "/usr/property/property.txt";

    String KEY_NAME_PORT = "listenning.port";
}
