package me.jar.http.server;

import org.junit.Test;

/**
 * @Description
 * @Date 2021/3/22-23:48
 */
public class HttpProxyTest {
    @Test
    public void testRunServer() {
        HttpProxy httpProxy = new HttpProxy(8080);
        httpProxy.run();
    }
}
