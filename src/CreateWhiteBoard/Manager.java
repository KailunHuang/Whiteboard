package CreateWhiteBoard;

import JoinWhiteBoard.UDPReceive;
import whiteboard.DShapeModel;
import whiteboard.Whiteboard;

import javax.annotation.processing.SupportedSourceVersion;
import javax.sql.rowset.spi.SyncResolver;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Manager {

    static JFrame frame;
    static JTable table;


    private static Hashtable<String, Integer> addresses = new Hashtable<>();
    private static Hashtable<String, Socket> socketList = new Hashtable<>();
    private static String[] columnNames = {"Online Users"};
    private static String[][] data = new String[10][1];
    private static JTextField sendArea;
    private static JButton btnClean;
    private static JScrollPane scrollPane;
    private static JTextField textField;
    private static JScrollPane ChatArea;
    private static JTextArea textArea;

    public static String InetIP = "192.168.43.200"; // 服务器的IP
    private static JMenuBar menuBar;

    public static Whiteboard whiteboard = null;

    private static int manager = 0;


    public static void main(String[] args) throws SocketException {
        InetIP = CreateWhiteBoard.InetIP.getV4IP();
        System.out.println("当前的IP是： " + InetIP);
        initialize();
        start();
    }

//    public Manager() {
//        initialize();
//        start();
//    }


    public static void start() {
        try {
            // -----注册方法👇------
            IjoinerAddresses ijoinerAddresses = new joinerAddresses(); // 这里用IjoinerAddresses创建的对象，因为IjoinerAddresses是父类，joinerAddresses是继承的子类
            // 这么做的原因是，java只允许单继承，而RMI需要继承UnicastRemoteObject和Remote。
            System.setProperty("java.rmi.server.hostname", InetIP);
//            Registry registry = LocateRegistry.getRegistry(); //这么创建默认端口就会出错
            Registry registry = LocateRegistry.createRegistry(1099); // 本地主机上的远程对象注册表Registry的实例,默认端口1099
//            System.setProperty("java.rmi.server.hostname", InetIP);
            registry.rebind("joinerAddresses", ijoinerAddresses); // 把远程对象注册到RMI注册服务器上，并命名为joinerAddresses
//            registry.rebind("rmi://" + InetIP + ":" + "1099" + "/joinerAddresses", ijoinerAddresses);
            // -----注册方法👆------

            // ------TCP部分👇----------
            ServerSocket serverSocket = new ServerSocket(8888); // 打开监听端口8888
            addresses.put("Manager : 8888", 1); // 把自己放入在线列表
            System.out.println("----监听中----");
            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 40,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(20)); //线程池

            //------接收message的线程👇------
            receiveMessageThread receiveMessageThread = new receiveMessageThread(8888 - 3000);
            threadPool.submit(receiveMessageThread);

            while (true) {
                Socket socket = serverSocket.accept(); //打开1个数据传输端
                String ip = socket.getInetAddress().getHostAddress();
                System.out.println("这是来自于 " + ip + ": " + socket.getPort());
                //------是否允许新的用户加入👇------
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();

                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                // 用户名: ip + socket.getPort
                Object[] options = {"Yes", "No"};
                int n = JOptionPane.showOptionDialog(frame,
                        "Would you like to approve the access?",
                        "New user " + ip + socket.getPort() + " applies to join",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options,
                        options[0]);
                if (n == 0) {
                    bufferedWriter.write("ack\n");
                    bufferedWriter.flush();
                } else {
                    bufferedWriter.write("denied\n");
                    bufferedWriter.flush();
                    continue;
                }

                send_update_address();

                //-----更新在线用户的地址👇-----
                addresses.put(ip + ":" + socket.getPort(), 1);
                socketList.put(ip + ":" + socket.getPort(), socket); //获取新加入用户的socket
                updateUsersAddresses(); // 向所有在线用户发送指令，更新用户在线用户信息
                updateTextTable(); // 刷新GUI上在线用户信息
                // printHashtable(addresses);
                //-----更新在线用户的地址👆-----

                //-----连接user的线程👇------
                dealThread deal = new dealThread(socket); // 这是扩展的Thread
                threadPool.submit(deal);
                //-----连接user的线程👆------
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void initialize() {
        frame = new JFrame();
//        frame.getContentPane().setBackground(new Color(0, 153, 102));
        frame.setBackground(Color.DARK_GRAY);
        frame.setBounds(100, 100, 580, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        scrollPane = new JScrollPane();
        scrollPane.setBounds(6, 6, 140, 282);
        frame.getContentPane().add(scrollPane);

        table = new JTable(data, columnNames);
        scrollPane.setViewportView(table);

        textField = new JTextField();
        textField.setBounds(6, 308, 140, 29);
        frame.getContentPane().add(textField);
        textField.setColumns(10);

        JButton btnKickOut = new JButton("Kick Out");

        btnKickOut.setBounds(6, 352, 140, 29);
        frame.getContentPane().add(btnKickOut);

        textArea = new JTextArea();
        textArea.setEditable(false);
        //textArea.setWrapStyleWord(true);
        textArea.setRows(20);
        textArea.setLineWrap(true);
        textArea.setColumns(1);
        //ChatArea.setColumnHeaderView(textArea);
        ChatArea = new JScrollPane(textArea);
        ChatArea.setBounds(158, 5, 401, 332);
        ChatArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(ChatArea);

        sendArea = new JTextField();
        sendArea.setBounds(158, 351, 288, 29);
        frame.getContentPane().add(sendArea);
        sendArea.setColumns(10);

        JButton btnSend = new JButton("Send");
        btnSend.setBounds(450, 354, 119, 25);
        frame.getContentPane().add(btnSend);

        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        ActionListener clean = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    kick_all();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        };

        ActionListener newWhiteboard = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (whiteboard == null) {
                        whiteboard = new Whiteboard(manager, 8888, InetIP);
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


                }
            }
        };

        JButton btnWhiteboard = new JButton("Whiteboard");
        menuBar.add(btnWhiteboard);

        btnClean = new JButton("Quit");
        menuBar.add(btnClean);
        btnClean.addActionListener(clean);
        btnWhiteboard.addActionListener(newWhiteboard);
        frame.setVisible(true);

        sendArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String message = "Manager: " + sendArea.getText() + "\n";
                    try {
                        sendMessages(message);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    textArea.setText(textArea.getText() + message);
                    sendArea.setText("");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        ActionListener kickout = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Kick Out")) {
                    String user = textField.getText();
                    Socket socket;
                    if ((socket = socketList.get(user)) != null) {
                        String ip = user.split(":")[0].trim();
                        int port = Integer.parseInt(user.split(":")[1].trim());
                        try {
                            //向目标用户发送一个被踢出的信息
                            UDPSend.kick(ip, port - 3000);
                            socket.close();
                            addresses.remove(user);
                            sendMessages(textField.getText() + " has been kicked out!\n");
                            updateTextTable();
                            socketList.remove(user);
                            updateUsersAddresses();
                            textField.setText("");

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        System.out.println("Kick out successfully");
                    }
                }
            }
        };
        btnKickOut.addActionListener(kickout);

        ActionListener sendMessage = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = "Manager: " + sendArea.getText() + "\n";
                try {
                    sendMessages(message);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                textArea.setText(textArea.getText() + message);
                sendArea.setText("");
            }
        };
        btnSend.addActionListener(sendMessage);
    }


    //--------更新user列表👇---------
    private static void updateUsersAddresses() throws IOException {
        if (addresses.size() == 0) {
            return;
        }

        for (Iterator<Map.Entry<String, Integer>> iterator = addresses.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            String str = entry.getKey();
            int port = Integer.parseInt(str.split(":")[1].trim());
            UDPSend.update(InetIP, port - 3000);
//            UDPSend.update(InetIP, port - 4000);
        }
    }

    private static void updateTextTable() throws IOException {  //更新GUI中在线用户列表
        data = new String[20][1]; // 在线用户列表中现实的内容
        if (addresses.size() == 0) {
            return;
        }
        // 把hahstable中的在线用户数据载入data
        int index = 0;
        for (Iterator<Map.Entry<String, Integer>> iterator = addresses.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            String str = entry.getKey();
            data[index][0] = str;
            index++;
        }
        // 刷新GUI的在线用户列表
        table = new JTable(data, columnNames);
        scrollPane.setViewportView(table);
    }

    private static void sendMessages(String message) throws IOException {
        if (addresses.size() == 0) {
            return;
        }
        for (Iterator<Map.Entry<String, Integer>> iterator = addresses.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            String str = entry.getKey();
            if (str.equals("Manager : 8888")) {
                continue;
            }
            String ip = str.split(":")[0].trim();
            int port = Integer.parseInt(str.split(":")[1].trim());

            UDPSend.sendMessage(ip, port - 3000, message);
        }
    }

    private static void send_update_address() throws IOException {
        if (addresses.size() == 0) {
            return;
        }
        for (Iterator<Map.Entry<String, Integer>> iterator = addresses.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            String str = entry.getKey();
            String ip = str.split(":")[0].trim();
            if (str.equals("Manager : 8888")) {
                ip = InetIP;
            }
            int port = Integer.parseInt(str.split(":")[1].trim());

            UDPSend.sendMessage(ip, port - 1500, ">>");
        }
    }

    public static void kick_all() throws IOException {
        if (addresses.size() == 0) {
            return;
        }
        for (Iterator<Map.Entry<String, Integer>> iterator = addresses.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            String str = entry.getKey();
            String ip = str.split(":")[0].trim();
            if (str.equals("Manager : 8888")) {
                continue;
            }
            int port = Integer.parseInt(str.split(":")[1].trim());

            UDPSend.over(ip, port - 3000);
        }
    }

    public static Hashtable<String, Integer> postHashtable() {
        return addresses;
    }


    public static void printHashtable(Hashtable<String, Integer> hashtable) {
        for (Iterator<Map.Entry<String, Integer>> iterator = hashtable.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            System.out.println(entry.getKey());
        }
    }

    public static void print_whiteboard_info(ArrayList<DShapeModel> arrayList) {
//        System.out.println("当前的whiteboard_info：");
        for (int i = 0; i < arrayList.size(); i++) {
            System.out.println(i + " : " + arrayList.get(i));
        }
    }

    //--------两个线程👇-----------
    static class receiveMessageThread extends Thread {
        private int port;

        public receiveMessageThread(int port) {
            this.port = port;
        }

        public synchronized void run() {
            try {
                while (true) {
                    String str = UDPReceive.receive(port);
                    System.out.println("收到了信息：" + str);
                    if (str.substring(0, 2).equals("/u")) {

                    } else if (str.substring(0, 2).equals("/q")) {
                        String quitUser = str.substring(2);
                        System.out.println("接收到退出请求：" + quitUser);
                        addresses.remove(quitUser);
                        printHashtable(addresses);
                        socketList.remove(quitUser);
                        updateTextTable();
                        updateUsersAddresses();
                    } else if (str.substring(0, 2).equals("/m")) {
                        str = str.substring(2);
                        textArea.setText(textArea.getText() + str);
                        textArea.setCaretPosition(textArea.getText().length());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class DShapePackage implements Serializable {
        public DShapeModel dShapeModel = null;
        public int index = -1;

        public DShapePackage(DShapeModel dShapeModel, int index) {
            this.dShapeModel = dShapeModel;
            this.index = index;
        }
    }

    //--------检测用户是否在线👇---------
    static class dealThread extends Thread {
        Socket client;

        public dealThread(Socket client) {
            this.client = client;
        }

        //使用同步的方法，防止多个线程对同一个对象的同一个实例变量进行操作时出现值不同步，值被更改的情况。
        public synchronized void run() {
            try {
                InputStream inputStream = client.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                //当线程没有被外部终止时
                boolean is_null_pre = false;
                boolean is_null_tmp = false;
                while (!Thread.interrupted()) {
                    String request = bufferedReader.readLine(); // 等待接收数据
//                    System.out.println("收到连接请求：" + request);
                    if (request != null) {
                        is_null_tmp = false;
                    } else {
                        is_null_tmp = true;
                    }
                    if (is_null_tmp && !is_null_pre) {
                        String address = client.getInetAddress().getHostAddress() + ":" + client.getPort();
                        System.out.println(address + " 断线了");
                        addresses.remove(address);
                        updateTextTable();
                        send_update_address();
                    } else if (!is_null_tmp && is_null_pre) {
                        String address = client.getInetAddress().getHostAddress() + ":" + client.getPort();
                        System.out.println(address + " 重连了");
                        addresses.put(address, 1);
                        updateTextTable();
                        send_update_address();
                    }
                    if (request != null) {
                        is_null_pre = false;
                    } else {
                        is_null_pre = true;
                    }
                    Thread.sleep(500);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("连接已断开，未传送成功！");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}