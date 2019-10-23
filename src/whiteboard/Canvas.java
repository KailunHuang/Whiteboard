package whiteboard;


import CreateWhiteBoard.IjoinerAddresses;
import CreateWhiteBoard.UDPSend;
import JoinWhiteBoard.UDPReceive;
import CreateWhiteBoard.Manager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import javax.swing.JPanel;

import CreateWhiteBoard.Manager.DShapePackage;

public class Canvas extends JPanel {

    public static Whiteboard board;
    private static DShape selected;
    private static Point pivotKnob;
    private static Point movingKnob;

    private static ArrayList<DShape> shapes;
    private Hashtable<Integer, DShape> shapeTable;
    private static ArrayList<Point> knobs;

    private int x = 0;
    private int y = 0;

    private Point p_start, p_drag;
    private int x_start, y_start;
    private int x_drag, y_drag;

    private static IjoinerAddresses remoteAddress;
    private static Registry registry;
    private static ArrayList<DShapeModel> whiteboard_info = new ArrayList<>();
    public static Hashtable<String, Integer> addresses = new Hashtable<>();


    public Canvas(Whiteboard board) throws ClassNotFoundException, RemoteException, NotBoundException {
        this.board = board;
        this.setBackground(Color.WHITE);
        canvasClicked();
        drag();
        setPreferredSize(new Dimension(400, 400));
        setBackground(Color.WHITE);
        shapes = new ArrayList<DShape>();
        selected = null;
        movingKnob = null;
        setVisible(true);


        System.out.println("服务器IP ：" + board.serverInetIP);
        registry = LocateRegistry.getRegistry(board.serverInetIP,1099);
        remoteAddress = (IjoinerAddresses) registry.lookup("joinerAddresses"); //从注册表中寻找joinerAddress method

        System.out.println("身份标示符：" + board.getMode());
        if (board.getMode() == board.manager) {
            System.out.println("现在是Manager");
            System.out.println("当前Local Port:" + board.LocalPort);
            addresses = remoteAddress.getAddressed();
            System.out.println("当前的在线用户为");
            Manager.printHashtable(addresses);
            //接受图形的线程
            receive_whiteboardInfo_Thread rw_thread = new receive_whiteboardInfo_Thread(8888 - 4000, this);
            rw_thread.start();
        } else {
            System.out.println("现在是Client");
            System.out.println("当前Local Port:" + board.LocalPort);
            updateThread update_Thread = new updateThread(board.LocalPort - 4000, this, remoteAddress);
            update_Thread.start();
        }
        //接收任意画的线程
        receive_draw_Thread draw_thread = new receive_draw_Thread(board.LocalPort - 5000, this);
        draw_thread.start();
    }

    public void canvasClicked() {

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {

                if (board.freehand == true || board.eraser == true) {
                    x_start = e.getX();
                    y_start = e.getY();
                }

                Point pt = e.getPoint();
                x = e.getX();
                y = e.getY();
                movingKnob = null;
                pivotKnob = null;
                System.out.println("鼠标被点击了");
                if (selected != null) {
                    System.out.println("select 不是null");
                    getSelection(pt);
                }


                if (movingKnob == null) {
                    selected = null;
                    for (int i = 0; i < shapes.size(); i++) {
                        if (shapes.get(i).containsPoint(pt))
                            selected = shapes.get(i);
                    }
                }
                repaint();
            }
        });

    }

    public void drag() {
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {

                if (board.freehand == true) {
                    x_drag = e.getX();
                    y_drag = e.getY();
                    p_start = new Point(x_start, y_start);
                    p_drag = new Point(x_drag, y_drag);

                    DLineModel model = new DLineModel(p_start, p_drag, board.penColor);
                    //System.out.println(board.freehandColor);
                    // Set Pen Color
                    model.setColor(board.penColor);
                    model.setStroke(board.Stroke);
                    try {
                        send_draw_info(model);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    try {
                        addShape(model);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    repaint();
                    x_start = x_drag;
                    y_start = y_drag;

                } else if (board.eraser == true) {
                    x_drag = e.getX();
                    y_drag = e.getY();
                    p_start = new Point(x_start, y_start);
                    p_drag = new Point(x_drag, y_drag);
                    DLineModel model = new DLineModel(p_start, p_drag);
                    //System.out.println(board.freehandColor);
                    // Set Pen Color
                    model.setColor(Color.WHITE);
                    model.setStroke(board.Stroke);
                    try {
                        addShape(model);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    repaint();
                    x_start = x_drag;
                    y_start = y_drag;

                } else if (board.getMode() != 2) {
                    int dx = e.getX() - x;
                    int dy = e.getY() - y;
                    x = e.getX();
                    y = e.getY();
                    System.out.println("selected shape: " + selected);
                    if (selected != null) {
                        selected.moveBy(dx, dy);

                        board.updateTable(selected);
                        //move
                        System.out.println("current shapes number:" + shapes.indexOf((selected)));
                        int index = shapes.indexOf(selected);


                        try {
                            send_update_whiteboard(index + 1);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }


                        repaint();

                    }

                    if (movingKnob != null) {
                        movingKnob.x += dx;
                        movingKnob.y += dy;
                        selected.resize(pivotKnob, movingKnob);

                    }

                }
//                Manager.print_whiteboard_info(whiteboard_info);


            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (board.freehand == true) {
                    board.freehand = false;
                    selected = null;
//                    System.out.println("Relased the mouse!");
//                    System.out.println("Freehand model: " + board.freehand);
                } else if (board.eraser == true) {
                    board.eraser = false;
                    selected = null;
//                    System.out.println("Relased the mouse!");
//                    System.out.println("Freehand model: " + board.freehand);
                } else {
                    int index = shapes.indexOf(selected);
                    System.out.println("传输图形给Server");
                    DShapePackage dShapePackage = new DShapePackage(selected.getModel(), index + 1);
                    try {
                        UDPSend.send_whiteboard_info(board.serverInetIP, 4888, dShapePackage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println("已发送图形");
                }
            }
        });
    }

    public void getSelection(Point pt) {
        knobs = selected.getKnobs();
        for (int i = 0; i < knobs.size(); i++) {
            Rectangle knob = new Rectangle(knobs.get(i).x - 4, knobs.get(i).y - 4, 9, 9);
            if (knob.contains(pt)) {
                int j = 0;
                movingKnob = new Point(knobs.get(i));
                if (knobs.size() == 2) {
                    if (i == 0)
                        j = 1;
                    else if (i == 1)
                        j = 0;
                } else {
                    if (i == 0)
                        j = 3;
                    else if (i == 1)
                        j = 2;
                    else if (i == 2)
                        j = 1;
                    else if (i == 3)
                        j = 0;
                }
                pivotKnob = new Point(knobs.get((j)));
                break;
            }
        }
    }

    public void recolorShape(Color color) {
        if (selected != null) {
            selected.setColor(color);
            //change color
            repaint();
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0; i < shapes.size(); i++) {
            DShape shape = shapes.get(i);
            shape.draw(g, (selected == shape));
        }

    }

    public void addShape(DShapeModel model) throws IOException {
        //System.out.println(model);
        model.setStroke(board.Stroke);
        if (board.getMode() != 2) {
            DShape shape = null;
            if (model instanceof DOvalModel)
                shape = new DOval(model);
            else if (model instanceof DTextModel)
                shape = new DText(model);
            else if (model instanceof DRectModel)
                shape = new DRect(model);
            else if (model instanceof DLineModel)
                shape = new DLine(model);
            shapes.add(shape);
            selected = shape;
            board.add(shape);

            whiteboard_info.add(shape.model);
            Manager.print_whiteboard_info(whiteboard_info);
            if (board.getMode() == board.client) {
                System.out.println("传输图形给Server");
                DShapePackage dShapePackage = new DShapePackage(model, 0);
                UDPSend.send_whiteboard_info(board.serverInetIP, 4888, dShapePackage);
                System.out.println("已发送图形");
            } else {
                if (!board.freehand) {
                    send_update_whiteboard(0);
                }
            }
            repaint();
        }
    }

    public void addShapeWhileReceive(DShapeModel model) throws IOException {
        if (board.getMode() != 2) {
            DShape shape = null;
            if (model instanceof DOvalModel)
                shape = new DOval(model);
            else if (model instanceof DTextModel)
                shape = new DText(model);
            else if (model instanceof DRectModel)
                shape = new DRect(model);
            else if (model instanceof DLineModel)
                shape = new DLine(model);
            shapes.add(shape);
//            selected = shape;
            board.add(shape);
            repaint();
        }

    }

    public void removeShape() {

        if (selected()) {
            shapes.remove(selected);
            board.delete(selected);
            whiteboard_info.remove(selected.model);
            Manager.print_whiteboard_info(whiteboard_info);
            selected = null;
            //发送删除
            repaint();
        }
    }

    //		overriding removeShape
    public void removeShape(DShape shape) {
        shapes.remove(shape);
        board.delete(shape);
        whiteboard_info.remove(shape.model);
        Manager.print_whiteboard_info(whiteboard_info);
        repaint();
    }

    public void updateShape(DShape shape, int index) {
        System.out.println("updating the shape");
        shapes.set(index, shape);
        board.updateModel(shape, index);
        whiteboard_info.set(index, shape.model);
        Manager.print_whiteboard_info(whiteboard_info);
        repaint();
    }

    public void toFront() {
        if (selected()) {
            shapes.remove(selected);
            shapes.add(selected);
            board.toFront(selected);
            //发送添加
            repaint();

        }
    }

    public void toBack() {
        if (selected()) {
            shapes.remove(selected);
            shapes.add(0, selected);
            board.toBack(selected);
            //发送删除
            repaint();

        }
    }

    public DShape getSelected() {
        return selected;
    }

    public List<DShape> getShapes() {
        return shapes;
    }

    public void setText(String text) {
        if (selected()) {
            ((DText) selected).setText(text);
            //发送修改text
            repaint();
        }

    }

    public void setFont(String fontName) {
        if (selected()) {
            ((DText) selected).setFont(fontName);
            //发送修改font
            repaint();
        }
    }

    public void setNull() {
        shapes.clear();
        board.clear();
        selected = null;
        repaint();
    }

    public boolean selected() {
        if (selected != null)
            return true;
        else
            return false;
    }

    public void sendEditShape() {
        if (board.getMode() == board.manager) { //manager

        } else { //other user

        }
    }

    public void sendDeleteShape() {
        if (board.getMode() == board.manager) { //manager

        } else { //other user

        }
    }

    public void sendAddShape(DShape shape) throws IOException {
        if (board.getMode() == board.manager) { //manager

        } else { //other user
//				UDPSend.send_whiteboard_info(joiner.InetIP, 4888, new Manager.DShapePackage(shape.model, 0));
        }
    }

    //-----------👇是通讯用到的方法和类---------------------//

    //服务端用来接收图形并放到ArrayList， 端口号-4000
    static class receive_whiteboardInfo_Thread extends Thread {
        private int port;
        private Canvas canvas;

        public receive_whiteboardInfo_Thread(int port, Canvas canvas) {
            this.port = port;
            this.canvas = canvas;
        }

        public synchronized void run() {
            try {
                DatagramSocket da = new DatagramSocket(port);
                while (true) {
                    Manager.DShapePackage dShapePackage = UDPReceive.receive_whiteboard_info(da);
                    System.out.println("收到了信息：" + dShapePackage.dShapeModel + ", " + dShapePackage.index);
                    if (dShapePackage.index == 0) { // 直接添加到whitboard_info
                        System.out.println("添加了图形");
                        canvas.addShape(dShapePackage.dShapeModel);
                    } else if (dShapePackage.index < 0) { //删除
                        canvas.removeShape(buildShapeByModel(dShapePackage.dShapeModel));
                    } else {//修改其中一个
                        int index = dShapePackage.index - 1;
                        System.out.println("current transfer index " + index);
                        whiteboard_info.set(index, dShapePackage.dShapeModel);
                        editShapeFromHashTable(whiteboard_info, index);
                    }
                    //让大家更新
                    Manager.print_whiteboard_info(whiteboard_info);
                    send_update_whiteboard(dShapePackage.index);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private static Hashtable<String, Integer> addresses = new Hashtable<>();

        public void deletShapesFromHashTable(int index) {
            int arraySize = canvas.shapes.size();
            if (index < arraySize) {
                DShape shape = canvas.shapes.get(index);
                canvas.removeShape(shape);
            }
        }

        public void editShapeFromHashTable(ArrayList<DShapeModel> arrayList, int index) {
            DShapeModel model = arrayList.get(index);
            DShape shape = buildShapeByModel(model);
            canvas.updateShape(shape, index);
        }

        public DShape buildShapeByModel(DShapeModel model) {
            DShape shape = null;
            if (model instanceof DOvalModel)
                shape = new DOval(model);
            else if (model instanceof DTextModel)
                shape = new DText(model);
            else if (model instanceof DRectModel)
                shape = new DRect(model);
            else if (model instanceof DLineModel)
                shape = new DLine(model);
            return shape;
        }
    }

    //客户端用来接收更新信号，端口号 -4000
    static class updateThread extends Thread {
        private int port;
        private Canvas canvas;
        private IjoinerAddresses remoteAddress;

        public updateThread(int port, Canvas canvas, IjoinerAddresses remoteAddress) {
            this.port = port;
            this.canvas = canvas;
            this.remoteAddress = remoteAddress;
        }

        public synchronized void run() {
            try {
                while (true) {
                    String str = UDPReceive.receive(port);  // 在此阻塞，等待一个UDP传输
                    System.out.println("Receive message: " + str);
                    if (str.substring(0, 2).equals("/w")) { // 如果收到的是/u，则会update在线用户信息
                        whiteboard_info = remoteAddress.get_whiteBoard_Info();
                        Manager.print_whiteboard_info(whiteboard_info);
                        canvas.setNull();
                        for (int i = 0; i < whiteboard_info.size(); i++) {
                            canvas.addShapeWhileReceive(whiteboard_info.get(i));
                        }

                    } else {
                        System.out.println("Wrong");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public DShape buildShapeByModel(DShapeModel model) {
            DShape shape = null;
            if (model instanceof DOvalModel)
                shape = new DOval(model);
            else if (model instanceof DTextModel)
                shape = new DText(model);
            else if (model instanceof DRectModel)
                shape = new DRect(model);
            else if (model instanceof DLineModel)
                shape = new DLine(model);
            return shape;
        }
    }

    //服务端用来发送更新信号
    public static void send_update_whiteboard(int index) throws IOException {

        if (addresses.size() == 0) {
            return;
        }
        for (Iterator<Map.Entry<String, Integer>> iterator = addresses.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            String str = entry.getKey();
            if (str.equals("Manager : 8888")) {
                continue;
            }
            System.out.println("通知 " + str + " 更新");
            String ip = str.split(":")[0].trim();
            int port = Integer.parseInt(str.split(":")[1].trim());

            UDPSend.update_whiteboard_table(ip, port - 4000, index);
        }
    }

    //接收任意画的信息并直接画，端口号-5000
    class receive_draw_Thread extends Thread {
        private int port;
        private Canvas canvas;

        public receive_draw_Thread(int port, Canvas canvas) {
            this.port = port;
            this.canvas = canvas;
        }

        public synchronized void run() {
            try {
                DatagramSocket da = new DatagramSocket(port);
                while (true) {
//                    System.out.println("接收线的端口是："+port);
                    DShapeModel dShapeModel = UDPReceive.receive_draw_info(da);
                    System.out.println("收到了draw的信息：" + dShapeModel);
                    // 直接画
                    DShape shape = new DLine(dShapeModel);
//                    canvas.addShapeWhileReceive(dShapeModel);
                    canvas.shapes.add(shape);
                    canvas.selected = shape;
                    canvas.board.add(shape);
                    repaint();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    //发送任意画的信息
    public static void send_draw_info(DLineModel item) throws IOException {
        System.out.println("在 send_draw_info 中");
        if (addresses.size() == 0) {
            return;
        }

        for (Iterator<Map.Entry<String, Integer>> iterator = addresses.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            String str = entry.getKey();
            String ip = str.split(":")[0].trim();
            if (str.equals("Manager : 8888")) {
                ip = board.serverInetIP;
            }
            int port = Integer.parseInt(str.split(":")[1].trim());

            UDPSend.send_draw_info(ip, port - 5000, item);
        }
    }

    //RMI
    public static ArrayList<DShapeModel> post_whiteboard_info() {
        return whiteboard_info;
    }


}
		
		
		
