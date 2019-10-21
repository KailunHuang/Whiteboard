package whiteboard;


import CreateWhiteBoard.IjoinerAddresses;
import CreateWhiteBoard.UDPSend;
import JoinWhiteBoard.UDPReceive;
import CreateWhiteBoard.Manager;
import JoinWhiteBoard.joiner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import javax.swing.JPanel;

import CreateWhiteBoard.Manager.DShapePackage;

public class Canvas extends JPanel {

    private Whiteboard board;
    private DShape selected;
    private Point pivotKnob;
    private Point movingKnob;

    private ArrayList<DShape> shapes;
    private Hashtable<Integer, DShape> shapeTable;
    private ArrayList<Point> knobs;

    private int x = 0;
    private int y = 0;

    private Point p_start, p_drag;
    private int x_start, y_start;
    private int x_drag, y_drag;

    private static IjoinerAddresses remoteAddress;
    private static Registry registry;
    private static ArrayList<DShapeModel> whiteboard_info = new ArrayList<>();

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
        registry = LocateRegistry.getRegistry(board.serverInetIP, 1099);
        remoteAddress = (IjoinerAddresses) registry.lookup("joinerAddresses"); //从注册表中寻找joinerAddress method
        System.out.println("身份标示符：" + board.getMode());
        if (board.getMode() == board.manager) {
            //接受图形的线程
            System.out.println("现在是Manager");
            receive_whiteboardInfo_Thread rw_thread = new receive_whiteboardInfo_Thread(4888, this);
            rw_thread.start();
        } else {
            System.out.println("当前Local Port:" + board.LocalPort);
            updateThread update_Thread = new updateThread(board.LocalPort - 4000, this);
            update_Thread.start();
        }
    }

    public void canvasClicked() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {

                if (board.freehand == true || board.eraser == true) {
                    x_start = e.getX();
                    y_start = e.getY();
                }

                if (board.getMode() != 2) {
                    Point pt = e.getPoint();
                    x = e.getX();
                    y = e.getY();
                    movingKnob = null;
                    pivotKnob = null;

                    if (selected != null) {
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
//	                    System.out.println(board.freehandColor);
                    // Set Pen Color
                    model.setColor(board.penColor);
                    model.setStroke(board.Stroke);
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
//	                    System.out.println(board.freehandColor);
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

                    if (selected != null) {
                        selected.moveBy(dx, dy);
                        board.updateTable(selected);
                        //move
                        repaint();
                    }

                    if (movingKnob != null) {
                        movingKnob.x += dx;
                        movingKnob.y += dy;
                        selected.resize(pivotKnob, movingKnob);

                    }

                }
                Manager.print_whiteboard_info(whiteboard_info);


            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (board.freehand == true) {
                    board.freehand = false;
                    selected = null;
                    System.out.println("Relased the mouse!");
                    System.out.println("Freehand model: " + board.freehand);
                } else if (board.eraser == true) {
                    board.eraser = false;
                    selected = null;
                    System.out.println("Relased the mouse!");
                    System.out.println("Freehand model: " + board.freehand);
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
//			System.out.println(model);
        model.setStroke(board.Stroke);
        System.out.println(board.Stroke);
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
                Manager.send_update_whiteboard(0);
            }

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

    static class updateThread extends Thread {
        private int port;
        private Canvas canvas;

        public updateThread(int port, Canvas canvas) {
            this.port = port;
            this.canvas = canvas;
        }

        public synchronized void run() {
            try {
                while (true) {
                    String str = UDPReceive.receive(port);  // 在此阻塞，等待一个UDP传输
                    System.out.println("Receive message: " + str);
                    if (str.substring(0, 2).equals("/w")) { // 如果收到的是/u，则会update在线用户信息
                        whiteboard_info = remoteAddress.get_whiteBoard_Info();
                        Manager.print_whiteboard_info(whiteboard_info);
                        ArrayList<DShape> newShapes = new ArrayList<>();
                        for (int i = 0; i < whiteboard_info.size(); i++) {
                            newShapes.add(buildShapeByModel(whiteboard_info.get(i)));
                        }
                        canvas.shapes = newShapes;
                        canvas.repaint();
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

    public static ArrayList<DShapeModel> post_whiteboard_info() {
        return whiteboard_info;
    }

    public void sendDeleteShape() {
        if (board.getMode() == board.manager) { //manager

        } else { //other user

        }
    }

//		public void sendAddShape(DShape shape) throws IOException {
//			if (board.getMode() == board.manager){ //manager
//
//			}else{ //other user
//				UDPSend.send_whiteboard_info(joiner.InetIP, 4888, new Manager.DShapePackage(shape.model, 0));
//			}
//		}

    //-----------THREAD CLASS---------------------//


    static class receive_whiteboardInfo_Thread extends Thread {
        private int port;
        private Canvas canvas;

        public receive_whiteboardInfo_Thread(int port, Canvas canvas) {
            this.port = port;
            this.canvas = canvas;
        }

        public synchronized void run() {
            try {
                while (true) {
                    Manager.DShapePackage dShapePackage = UDPReceive.receive_whiteboard_info(port);
                    System.out.println("收到了信息：" + dShapePackage.dShapeModel + ", " + dShapePackage.index);
                    if (dShapePackage.index == 0) { // 直接添加到whitboard_info
                        System.out.println("添加了图形");
                        canvas.addShape(dShapePackage.dShapeModel);
                    } else if (dShapePackage.index < 0) { //删除
                        whiteboard_info.remove(-1 * dShapePackage.index);
                        canvas.removeShape(buildShapeByModel(dShapePackage.dShapeModel));
                    } else {//修改其中一个
                        whiteboard_info.set(dShapePackage.index, dShapePackage.dShapeModel);
                        editShapeFromHashTable(whiteboard_info, dShapePackage.index);
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

        public static void send_update_whiteboard(int index) throws IOException {
            addresses = remoteAddress.getAddressed();
            Manager.printHashtable(addresses);
            if (addresses.size() == 0) {
                return;
            }
            for (Iterator<Map.Entry<String, Integer>> iterator = addresses.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Integer> entry = iterator.next();
                String str = entry.getKey();
                System.out.println("通知 " + str + " 更新");
                if (str.equals("Manager : 8888")) {
                    continue;
                }
                String ip = str.split(":")[0].trim();
                int port = Integer.parseInt(str.split(":")[1].trim());

                UDPSend.update_whiteboard_table(ip, port - 4000, index);
            }
        }

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


}
		
		
		
