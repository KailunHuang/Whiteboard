package whiteboard;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDP_Object_Receiver {
    static DShapeModel dShapeModel = null;

    public static void main(String[] args) throws ClassNotFoundException {
        try {
            System.out.println("等待接收...");
            DatagramSocket da = new DatagramSocket(9099);
            byte[] bytes = new byte[1024 * 1024];
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
            da.receive(datagramPacket);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            dShapeModel = (DShapeModel) objectInputStream.readObject();
            System.out.println("接收完毕 "+dShapeModel);
            System.out.println("等待接收...");
            datagramPacket = new DatagramPacket(bytes, bytes.length);
            da.receive(datagramPacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static DShapeModel getdShapeModel() throws ClassNotFoundException {
        main(null);
        return dShapeModel;
    }
}
