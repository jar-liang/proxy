package me.jar.constants;

import me.jar.utils.PlatformUtil;

import java.util.Map;

/**
 * @Description
 * @Date 2021/4/21-22:12
 */
public interface ProxyConstants {

    byte[] DELIMITER = new byte[] {13, 13, 13, 13, 10, 10, 10, 10};

    /**
     * 特定标识字节，用于标识数据流是否合法源发出
     */
    byte[] MARK_BYTE = new byte[] {8, 8, 8, 8};

    int MAX_FRAME_LENGTH = 128 * 1024;

    int WIN_OS = 1;

    int LINUX_OS = 2;

    int OTHER_OS = 3;

    String PROPERTY_NAME_WIN = "D:\\usr\\property\\property.txt";

    String PROPERTY_NAME_LINUX= "/usr/property/property.txt";

    String KEY_NAME_PORT = "listenning.port";

    Map<String, String> PROPERTY = PlatformUtil.getProperty();

    String PROPERTY_NAME_KEY = "key";
}
