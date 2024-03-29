package JoinWhiteBoard;
import whiteboard.DLineModel;
import whiteboard.DShapeModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import CreateWhiteBoard.Manager.DShapePackage;

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

    public static DShapePackage receive_whiteboard_info(DatagramSocket da) throws IOException, ClassNotFoundException {
        System.out.println("等待接收 whiteboard_info...");
        byte[] bytes = new byte[1024 * 1024];
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
        da.receive(datagramPacket);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        DShapePackage dShapeModel = (DShapePackage) objectInputStream.readObject();
        System.out.println("接收完毕 " + dShapeModel);
//        da.close();
        return dShapeModel;
    }

    public static DShapeModel receive_draw_info(DatagramSocket da) throws IOException, ClassNotFoundException {
        System.out.println("等待接收 draw...");
        byte[] bytes = new byte[1024 * 1024];
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
        da.receive(datagramPacket);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        DLineModel dLineModel = (DLineModel) objectInputStream.readObject();
        System.out.println("接收完毕 " + dLineModel);
//        da.close();
        return dLineModel;
    }


}