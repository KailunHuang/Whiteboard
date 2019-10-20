package CreateWhiteBoard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UDPSend {
    public static void update(String ip, int port) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        String str = "/u";
        byte[] datagram = str.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(datagram, datagram.length);
        datagramPacket.setSocketAddress(new InetSocketAddress(ip, port));
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }

    public static void kick(String ip, int port) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        String str = "/k";
        byte[] datagram = str.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(datagram, datagram.length);
        datagramPacket.setSocketAddress(new InetSocketAddress(ip, port));
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }

    public static void quit(String ip, String message) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        String str = "/q" + message;
        System.out.println("发出退出请求：" + str);
        byte[] datagram = str.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(datagram, datagram.length);
        datagramPacket.setSocketAddress(new InetSocketAddress(ip, 5888));
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }

    public static void sendMessage(String ip, int port, String message) throws IOException { // 发送message UDP
        DatagramSocket datagramSocket = new DatagramSocket(); // 新建一个数据包的socket对象
        message = "/m" + message;
        byte[] datagram = message.getBytes();   // 数据包是byte格式
        DatagramPacket datagramPacket = new DatagramPacket(datagram, datagram.length); //把数据打包
        datagramPacket.setSocketAddress(new InetSocketAddress(ip, port)); // 通过socket发送
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }
}