package JoinWhiteBoard;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import CreateWhiteBoard.IjoinerAddresses;
import CreateWhiteBoard.UDPSend;
import whiteboard.Whiteboard;

import javax.swing.*;

/**
 * This class retrieves a reference to the remote object from the RMI registry. It
 * invokes the methods on the remote object as if it was a local object of the type of the
 * remote interface.
 */
public class joiner {
    private static String[] columnNames = {"Online Users"};
    private static Hashtable<String, Integer> hashtable;
    private static String[][] data = new String[10][1];
    private static IjoinerAddresses remoteAddress;
    private static JTextField sendArea;
    private static JButton btnQuit;
    private static JScrollPane scrollPane;
    private static JScrollPane ChatArea;
    private static Registry registry;
    private static JTextArea textArea;
    private static Socket socket;
    private static JFrame frame;
    private static JTable table;
//    private static String InetIP;

    private static String InetIP = "192.168.43.200"; //服务端IP
    // 凯凯: 192.168.43.175 小陆: 192.168.43.200


    public static int LocalPort = 0;
    private static JButton btnWhiteboard;
    private static JMenuBar menuBar;
    private static Whiteboard whiteboard;

    private static int client = 1;


//    public static void main(String[] args) throws SocketException {

//        joinerInitialize window = new joinerInitialize();
//        initialize();
//        start();
//    }


    public joiner(String IPAddress, Socket socketClient) throws SocketException, IOException {
        InetIP = CreateWhiteBoard.InetIP.getV4IP();
        System.out.println("当前IP是： " + InetIP);
        InetIP = IPAddress;
        socket = socketClient;

        initialize();
        start();
        connect_test(socketClient);
    }

    public static void connect_test(Socket socketClient) {
        Thread connect_test_thread = new Thread() {
            public void run() {
                try {
                    OutputStream outputStream = socketClient.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    while (true) {
                        bufferedWriter.write("> \n");
                        bufferedWriter.flush();
                        Thread.sleep(1000);
                    }
                } catch (IOException | InterruptedException e) {

                }
            }
        };
        connect_test_thread.start();
    }

    public static void start() {

        try {
//            System.out.println("目标IP是：" + InetIP);

            //-------传输画板数据👇---------
//            OutputStream outputStream = socket.getOutputStream();
//            InputStream inputStream = socket.getInputStream();
//            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//            String ServerAns = bufferedReader.readLine();
//
//            System.out.println("成功与Server建立连接!");
            //-------传输数据👆---------

            //------RMI👇-------
            //连接到注册表
            registry = LocateRegistry.getRegistry(InetIP, 1099);
            System.out.println(InetIP);
            remoteAddress = (IjoinerAddresses) registry.lookup("joinerAddresses"); //从注册表中寻找joinerAddress method
            hashtable = remoteAddress.getAddressed(); //得到该method return的数据
            hashtable.remove((socket.getLocalAddress() + ":" + socket.getLocalPort()).substring(1)); //把自己的地址从hashtable中删除
            LocalPort = socket.getLocalPort();
            printHashtable(hashtable);
            updateTextTable(); //更新GUI中的用户列表
            //------RMI👆-------/

            //---------更新在线用户👇----------
            updateThread updateThread = new updateThread(socket.getLocalPort() - 3000);
            updateThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void initialize() {
        frame = new JFrame();
//        frame.getContentPane().setBackground(new Color(0, 153, 102));
        frame.setBounds(100, 100, 575, 490);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        scrollPane = new JScrollPane();
        scrollPane.setBounds(6, 6, 136, 406);
        frame.getContentPane().add(scrollPane);

        table = new JTable(data, columnNames);
        scrollPane.setViewportView(table);

        sendArea = new JTextField();
        sendArea.setBounds(154, 386, 303, 29);
        frame.getContentPane().add(sendArea);
        sendArea.setColumns(10);

        JButton btnSend = new JButton("Send");
        btnSend.setBounds(456, 389, 119, 25);
        frame.getContentPane().add(btnSend);


        textArea = new JTextArea();
        textArea.setEditable(false);
        //textArea.setWrapStyleWord(true);
        textArea.setRows(20);
        textArea.setLineWrap(true);
        textArea.setColumns(1);
        //ChatArea.setColumnHeaderView(textArea);
        ChatArea = new JScrollPane(textArea);
        ChatArea.setBounds(154, 6, 415, 368);
        ChatArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(ChatArea);

        ActionListener quit = new ActionListener() {  // 退出聊天
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String message = "Me " + ":" + socket.getLocalPort();
                    UDPSend.quit(InetIP, message);
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        };

        ActionListener newWhiteboard = new ActionListener() {  // 新建whiteboard
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    if (whiteboard == null) {
                        whiteboard = new Whiteboard(client, LocalPort, InetIP);
                    } else {
                        whiteboard.board.setVisible(true);
                    }
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                } catch (NotBoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };

        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        btnWhiteboard = new JButton("Whiteboard");
        menuBar.add(btnWhiteboard);
        btnWhiteboard.addActionListener(newWhiteboard);

        btnQuit = new JButton("Quit");
        menuBar.add(btnQuit);
        btnQuit.addActionListener(quit);
        frame.setVisible(true);

        sendArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String message = socket.getLocalPort() + " : " + sendArea.getText() + "\n"; // 得到当前用户的地址和所要发送的信息
                    try {
                        updateChatTable(message); // 把该信息投放在聊天界面
                        sendArea.setText(""); // 清空发送信息框
                        sendMessages(message); // 把该信息发送给所有在线用户
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });


        ActionListener sendMessage = new ActionListener() { //用来发送信息
            public void actionPerformed(ActionEvent e) {
                String message = socket.getLocalPort() + " : " + sendArea.getText() + "\n"; // 得到当前用户的地址和所要发送的信息
                try {
                    updateChatTable(message); // 把该信息投放在聊天界面
                    sendArea.setText(""); // 清空发送信息框
                    sendMessages(message); // 把该信息发送给所有在线用户
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
        btnSend.addActionListener(sendMessage);
    }

    public static void updateChatTable(String message) {  //更新GUI中在线用户列表
        // 把hahstable中的在线用户数据载入data
        textArea.setText(textArea.getText() + message);
        textArea.setCaretPosition(textArea.getText().length());
    }

    public static void updateTextTable() {  //更新GUI中在线用户列表
        data = new String[20][1]; // 在线用户列表中现实的内容
        if (hashtable.size() == 0) {
            return;
        }
        // 把hahstable中的在线用户数据载入data
        int index = 0;
        for (Iterator<Map.Entry<String, Integer>> iterator = hashtable.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            String str = entry.getKey();
            data[index][0] = str;
            index++;
        }
        // 刷新GUI的在线用户列表
        table = new JTable(data, columnNames);
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.setValue(index);
        scrollPane.setViewportView(table);
    }

    public static void sendMessages(String message) throws IOException {  // 将当前用户的信息发送给所有在线用户
        if (hashtable.size() == 0) { // 当没有用户的时候，不发送信息
            return;
        }
        UDPSend.sendMessage(InetIP, 8888 - 3000, message);
        for (Iterator<Map.Entry<String, Integer>> iterator = hashtable.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            String str = entry.getKey();
//            System.out.println(str);
            String ip = str.split(":")[0].trim();
            if (ip.equals("Manager")) {
                continue;
            }
            int port = Integer.parseInt(str.split(":")[1].trim()); // 得到port number
            UDPSend.sendMessage(ip, port - 3000, message); //发送信息
        }
    }

    static class updateThread extends Thread {
        private int port;

        public updateThread(int port) {
            this.port = port;
        }

        public synchronized void run() {
            try {
                while (true) {
                    String str = UDPReceive.receive(port);  // 在此阻塞，等待一个UDP传输
                    System.out.println("Receive message: " + str);
                    if (str.substring(0, 2).equals("/u")) { // 如果收到的是/u，则会update在线用户信息
                        hashtable = remoteAddress.getAddressed(); // 从Manager得到全新的在线用户列表
//                        hashtable.remove((socket.getLocalAddress() + ":" + socket.getLocalPort()).substring(1));// 把自己删除
                        updateTextTable(); // 更新GUI上的在线用户列表
                        // printHashtable(hashtable);
                    } else if (str.substring(0, 2).equals("/k")) { // 如果收到的是/k，则表示被踢出了房间
                        socket.close(); // 连接的socket关闭
                        JOptionPane.showMessageDialog(null, " You has been kicked out by manager! ", " Notice", JOptionPane.ERROR_MESSAGE);
                        System.exit(0); // 退出程序
                        System.out.println("Has been kicked out");
                    } else if (str.substring(0, 2).equals("/m")) {
                        str = str.substring(2);
                        updateChatTable(str);
                    } else if (str.substring(0, 2).equals("/o")) { // 如果收到的是/o，则表示Manager关闭了画板服务
                        socket.close(); // 连接的socket关闭
                        JOptionPane.showMessageDialog(null, " The server has been closed! ", " Error", JOptionPane.ERROR_MESSAGE);//提示用户manager已经退出程序
                        System.out.println("Has been closed!");
                        System.exit(0); // 退出程序
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void printHashtable(Hashtable<String, Integer> hashtable) {
        for (Iterator<Map.Entry<String, Integer>> iterator = hashtable.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
        }
    }
}