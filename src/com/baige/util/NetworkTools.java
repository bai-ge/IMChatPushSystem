package com.baige.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 */
public class NetworkTools {

    /*   因此需要确保在/etc/hosts文件中存在着这么一条映射：
        <hostname> <local_ip>
   */
/*<hostname>为你在终端执行hostname返回的本机名，
 <local_ip>则为在终端执行ifconfig得到的本机真实IP，
 JAVA提供的这个函数会去这个文件中找<hostname>对应的IP地址，
 不然会因为找不到而抛错。确保这一条之后，在linux上也能正确检测指定端口的占用情况~
 */
    public static void bindPort(String host, int port) throws Exception {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(host, port));
        s.close();
    }


    /**
     * 判断该端口能否使用
     *
     * @param port
     * @return
     */
    public static boolean isPortAvailable(int port) {
        Socket s = new Socket();
        try {
            bindPort("0.0.0.0", port);
            bindPort(InetAddress.getLocalHost().getHostAddress(), port);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
