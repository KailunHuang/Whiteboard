package JoinWhiteBoard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPReceive {
    // 用来接受manager发来的 kick out 或者update 的指令
    public static String receive(int port) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(port);
        byte[] datagram = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(datagram, datagram.length);
        datagramSocket.receive(datagramPacket);
        // 去除datagram多余的0
        int length = 0;
        for (int i = 0; i < datagram.length; ++i) {
            if (datagram[i] == 0) {
                length = i;
                break;
            }
        }
        System.out.println("接收到的有效字段长度为：" + length);
        String str = new String(datagram, 0, length, "UTF-8");
        datagramSocket.close();
        return str;
    }
}