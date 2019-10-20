package CreateWhiteBoard;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InetIP {
    private static final Integer TIME_OUT = 1000;

    static String INTRANET_IP = "127.0.0.1";

    public static String getIntranetIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getV4IP() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                String str = i.getHostAddress();
                if ((str.charAt(0) != 'f') && (str.charAt(0) != '0') && !(str.equals("127.0.0.1"))) {
                    return  i.getHostAddress();
                }
            }
        }
        return "127.0.0.1";
    }
}
