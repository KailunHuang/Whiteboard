import whiteboard.DOval;
import whiteboard.DShape;
import whiteboard.DShapeModel;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UDP_Object_Sender {
    public static void main(String[] args) {
        try {
            DShapeModel item = new DShapeModel(1, 2, 3, 4, Color.black);
            System.out.println(item);
//            DShape item = new DOval(dShapeModel);
            DatagramSocket datagramSocket = new DatagramSocket();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(item);

            byte[] bytes = byteArrayOutputStream.toByteArray();
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, new InetSocketAddress("localhost", 9099));
            datagramSocket.send(datagramPacket);
            System.out.println("已发送" + item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


