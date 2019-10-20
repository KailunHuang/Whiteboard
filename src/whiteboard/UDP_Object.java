package whiteboard;

import CreateWhiteBoard.IjoinerAddresses;
import JoinWhiteBoard.UDPReceive;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Hashtable;

public class UDP_Object {
    private static IjoinerAddresses remoteAddress;
    private static Registry registry;
    private static Hashtable<Integer, DShapeModel> whiteboard_info = new Hashtable<>();



    static class createThread extends Thread {
        private int port;
        private String InetIP;

        public createThread(String InetIP, int port) {
            this.port = port;
            this.InetIP = InetIP;
        }

        public synchronized void run() {
            try {
                registry = LocateRegistry.getRegistry(InetIP, 1099);
                remoteAddress = (IjoinerAddresses) registry.lookup("joinerAddresses"); //从注册表中寻找joinerAddress method
                while (true) {
                    String str = UDPReceive.receive(port);
                    System.out.println("收到了信息：" + str);
                    if (str.charAt(2) == '+') { // 这里是单纯的添加了新的图形
                        whiteboard_info = remoteAddress.get_whiteBoard_Info();
                    } else { //修改某一个图形
                        int motified_index = Integer.parseInt(str.substring(2));

                    }
                }
            } catch (IOException | NotBoundException e) {
                e.printStackTrace();
            }
        }
    }
}
