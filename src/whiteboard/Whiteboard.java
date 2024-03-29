package whiteboard;


import CreateWhiteBoard.IjoinerAddresses;
import CreateWhiteBoard.Manager;
import CreateWhiteBoard.UDPSend;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
//import javafx.scene.layout.VBox;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class Whiteboard extends JFrame {


    // Initial the Components
    public static JFrame board;
    private JTable table;
    private JTextField textField;
    //    private JScrollPane scrollpane;
    private JComboBox fontSelector;

    // Initial the variables
    private Canvas canvas;
    public TableModel tableModel;
    private HashMap<String, Integer> fontMap;
    private static IjoinerAddresses remoteAddress;
    private static Registry registry;
    public static Hashtable<String, Integer> addresses = new Hashtable<>();

    private int mode;
    public static int manager = 0;
    public static int client = 1;


    public boolean freehand = false;
    public boolean eraser = false;
    public Color penColor;
    public int Stroke = 3;
    public int LocalPort = 0;
    public String serverInetIP;
    public DTextModel model;

    public Whiteboard(int mode, int Localport, String InetIP) throws ClassNotFoundException, IOException, NotBoundException {

        this.mode = mode;
        this.LocalPort = Localport;
        this.serverInetIP = InetIP;
        board = new JFrame("Whiteboard");
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas = new Canvas(this);

        // Initial the Font Components
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fonts[] = g.getAvailableFontFamilyNames();
        fontMap = new HashMap<String, Integer>();
        for (int i = 0; i < fonts.length; i++) {
            fontMap.put(fonts[i], i);
        }

        /*
         * Initial the GUI Components
         */

        // Table
        tableModel = new TableModel();
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

//        // ScrollPane
//        scrollpane = new JScrollPane(table);
//        scrollpane.setPreferredSize(new Dimension(380, 400));

        // Initial MenuBar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        board.setJMenuBar(menuBar);

        // MenuFile
        JMenu mnFile = new JMenu("File");
        mnFile.setBackground(Color.WHITE);

        menuBar.add(mnFile);

        JMenuItem mntmNew = new JMenuItem("New");
        mntmNew.setBackground(Color.WHITE);
        mnFile.add(mntmNew);

        JMenu mnOpen = new JMenu("Open");
        mnFile.add(mnOpen);

        JMenuItem mntmOpen = new JMenuItem("Open");
        mnOpen.add(mntmOpen);

        JMenuItem mntmOpenInSystem = new JMenuItem("Open In ...");
        mnOpen.add(mntmOpenInSystem);

        // Save File Operations
        JMenu mnSavefile = new JMenu("Save File");
        mnFile.add(mnSavefile);

        JMenuItem mntmSaveFile = new JMenuItem("Save File (Default)");
        mnSavefile.add(mntmSaveFile);

        JMenuItem mntmSaveFileAs = new JMenuItem("Save File As ...");
        mnSavefile.add(mntmSaveFileAs);

        // whether the mode is manager
        if (mode == client) {
            mnFile.setVisible(false);
        }

        // Save Image Operations
        JMenu mnSaveImage = new JMenu("Save Image");
        mnFile.add(mnSaveImage);

        JMenuItem mntmSaveImage = new JMenuItem("Save Image (Default)");
        mnSaveImage.add(mntmSaveImage);

        JMenuItem mntmSaveIamgeAs = new JMenuItem("Save Image As ...");
        mnSaveImage.add(mntmSaveIamgeAs);

        // MenuEdit
        JMenu mnEdit = new JMenu("Edit");
        mnEdit.setBackground(Color.WHITE);
        menuBar.add(mnEdit);

        JMenuItem mntmDelete = new JMenuItem("Delete");
        mnEdit.add(mntmDelete);

        JMenuItem mntmClearAll = new JMenuItem("Clear All");
        mnEdit.add(mntmClearAll);

//		JMenuItem mntmSetColor = new JMenuItem("Reset Color");
//		mnEdit.add(mntmSetColor);

        JMenuItem mntmMoveToBack = new JMenuItem("Move to Back");
        mnEdit.add(mntmMoveToBack);

        JMenuItem mntmMoveToFront = new JMenuItem("Move to Front");
        mnEdit.add(mntmMoveToFront);


        // MenuAddShape
        JMenu mnAddShape = new JMenu("Add Shape");
        mnAddShape.setBackground(Color.WHITE);
        menuBar.add(mnAddShape);

        JMenuItem mntmAddLine = new JMenuItem("Add Line");
        mnAddShape.add(mntmAddLine);

        JMenuItem mntmAddOval = new JMenuItem("Add Oval");
        mnAddShape.add(mntmAddOval);

        JMenuItem mntmAddRectangle = new JMenuItem("Add Rectangle");
        mnAddShape.add(mntmAddRectangle);

        // MenuText
        JMenu mnText = new JMenu("Text");
        mnText.setBackground(Color.WHITE);
        menuBar.add(mnText);

        JMenuItem mntmAddText = new JMenuItem("Add Text");
        mnText.add(mntmAddText);

        JPanel panelSpace = new JPanel();
        panelSpace.setBackground(Color.WHITE);
        panelSpace.setMaximumSize(new Dimension(20, 20));
        panelSpace.setPreferredSize(new Dimension(20, 20));
        menuBar.add(panelSpace);

        JLabel lblTextFont = new JLabel("Text Font: ");
        lblTextFont.setBackground(Color.WHITE);
        lblTextFont.setMaximumSize(new Dimension(80, 20));
        lblTextFont.setPreferredSize(new Dimension(80, 20));
        menuBar.add(lblTextFont);

        // Text field
        textField = new JTextField("Sample Text");
        textField.setMaximumSize(new Dimension(120, 20));
        textField.setPreferredSize(new Dimension(120, 20));
        menuBar.add(textField);

        // Font ComboBox
        fontSelector = new JComboBox(fonts);
        fontSelector.setMaximumSize(new Dimension(130, 20));
        fontSelector.setPreferredSize(new Dimension(130, 20));
        menuBar.add(fontSelector);

        // Control Box
        Box controlGroup = Box.createVerticalBox();
        Box addGroup = Box.createHorizontalBox();
        Box functionGroup = Box.createHorizontalBox();
        Box tableGroup = Box.createHorizontalBox();
        Box networkingGroup = Box.createHorizontalBox();
        Box utilityGroup = Box.createHorizontalBox();

        // Shortcut Button
        JButton btnFreeHand = new JButton("");
        btnFreeHand.setIcon(new ImageIcon(Whiteboard.class.getResource("/resources/freehand.png")));
        btnFreeHand.setMaximumSize(new Dimension(30, 30));
        btnFreeHand.setPreferredSize(new Dimension(30, 30));

        JButton buttonEraser = new JButton("");
        buttonEraser.setIcon(new ImageIcon(Whiteboard.class.getResource("/resources/eraser.png")));
        buttonEraser.setMaximumSize(new Dimension(30, 30));
        buttonEraser.setPreferredSize(new Dimension(30, 30));

        JButton btnfunction = new JButton("");
        btnfunction.setIcon(new ImageIcon(Whiteboard.class.getResource("/resources/cursor.png")));
        btnfunction.setMaximumSize(new Dimension(30, 30));
        btnfunction.setPreferredSize(new Dimension(30, 30));

        JButton btnAddLine = new JButton("");
        btnAddLine.setIcon(new ImageIcon(Whiteboard.class.getResource("/resources/line.png")));
        btnAddLine.setMaximumSize(new Dimension(30, 30));
        btnAddLine.setPreferredSize(new Dimension(30, 30));

        JButton btnColor = new JButton("Color");
        menuBar.add(btnColor);

        JButton btnAddOval = new JButton("");
        btnAddOval.setIcon(new ImageIcon(Whiteboard.class.getResource("/resources/oval.png")));
        btnAddOval.setMaximumSize(new Dimension(30, 30));
        btnAddOval.setPreferredSize(new Dimension(30, 30));

        JButton btnAddText = new JButton("");
        btnAddText.setIcon(new ImageIcon(Whiteboard.class.getResource("/resources/text.png")));
        btnAddText.setMaximumSize(new Dimension(30, 30));
        btnAddText.setPreferredSize(new Dimension(30, 30));

        JButton btnAddRectangle = new JButton("");
        btnAddRectangle.setIcon(new ImageIcon(Whiteboard.class.getResource("/resources/rectangle.png")));
        btnAddRectangle.setMaximumSize(new Dimension(30, 30));
        btnAddRectangle.setPreferredSize(new Dimension(30, 30));

        JButton btnClose = new JButton("");
        btnClose.setIcon(new ImageIcon(Whiteboard.class.getResource("/resources/close.png")));
        btnClose.setMaximumSize(new Dimension(30, 30));
        btnClose.setPreferredSize(new Dimension(30, 30));

        String[] lineWidth = new String[]{"3", "5", "7", "9", "11"};
        JComboBox WidthChooser = new JComboBox(lineWidth);
        WidthChooser.setMaximumSize(new Dimension(80, 30));
        WidthChooser.setPreferredSize(new Dimension(80, 30));

        // Add Components into Control Box
        utilityGroup.add(btnfunction);
        utilityGroup.add(btnFreeHand);
        utilityGroup.add(buttonEraser);
        utilityGroup.add(btnAddLine);
        utilityGroup.add(btnAddOval);
        utilityGroup.add(btnAddRectangle);
        utilityGroup.add(btnAddText);
        utilityGroup.add(btnClose);
        utilityGroup.add(WidthChooser);

        controlGroup.add(utilityGroup);
        controlGroup.add(functionGroup);
        controlGroup.add(networkingGroup);
        controlGroup.add(tableGroup);
        controlGroup.add(addGroup);

        /*
         * Listener
         */

        btnAddLine.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eraser = false;
                freehand = false;
                Point p1 = new Point(25, 25);
                Point p2 = new Point(75, 75);
                DLineModel model = new DLineModel(p1, p2, penColor);

                //model.setPoints(p1,p2);
                try {
                    canvas.addShape(model);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                canvas.repaint();
            }
        });

        btnAddOval.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                DOvalModel model = new DOvalModel(25, 25, 100, 100, penColor);
                try {
                    canvas.addShape(model);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                canvas.repaint();
                eraser = false;
                freehand = false;
            }
        });

        btnfunction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eraser = false;
                freehand = false;
                freehand = false;
                eraser = false;
            }
        });

        btnAddRectangle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eraser = false;
                freehand = false;
                DRectModel model = new DRectModel(25, 25, 100, 100, penColor);
                try {
                    canvas.addShape(model);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                canvas.repaint();
            }
        });

        btnAddText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eraser = false;
                freehand = false;
                String s = textField.getText();
                DTextModel model = new DTextModel(s);
                model.setBounds(20, 20, 415, 80);
                try {
                    canvas.addShape(model);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                canvas.repaint();
            }
        });

        btnFreeHand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                freehand = true;
            }
        });

        buttonEraser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eraser = true;
                freehand = false;
            }
        });

        btnColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                penColor = JColorChooser.showDialog(Whiteboard.this, "Set Pen Color", Color.BLACK);
//				System.out.println(penColor);
                if (canvas.selected()) {
//					Color color = JColorChooser.showDialog(Whiteboard.this, "Set Color", canvas.getSelected().getColor());
//						System.out.println(color);
                    try {
                        canvas.recolorShape(penColor);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        WidthChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eraser = false;
                freehand = false;
                int s = (int) WidthChooser.getSelectedIndex();
                Stroke = s + 1;
                System.out.println(Stroke);
            }
        });
        ;

        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                board.setVisible(false);
                eraser = false;
                freehand = false;
            }
        });

        // Change the Font by this function

        fontSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (canvas.getSelected() instanceof DText)
                    canvas.setFont((String) fontSelector.getSelectedItem());
            }
        });

        // Change the Text by this function

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                handleTextChange(e);

                try {
                    if (getMode() == manager) {
                        canvas.send_update_whiteboard(0);
                    } else {
                        Manager.DShapePackage dShapePackage = new Manager.DShapePackage(model, 0);
                        UDPSend.send_whiteboard_info(serverInetIP, 4888, dShapePackage);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            public void insertUpdate(DocumentEvent e) {
                handleTextChange(e);
                try {
                    if (getMode() == manager) {
                        canvas.send_update_whiteboard(0);
                    }
//                    else {
//                        Manager.DShapePackage dShapePackage = new Manager.DShapePackage(model, 0);
//                        UDPSend.send_whiteboard_info(serverInetIP, 4888, dShapePackage);
//                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            public void removeUpdate(DocumentEvent e) {
                handleTextChange(e);

                try {
                    if (getMode() == manager) {
                        canvas.send_update_whiteboard(0);
                    }
//                    else {
//                        Manager.DShapePackage dShapePackage = new Manager.DShapePackage(model, 0);
//                        UDPSend.send_whiteboard_info(serverInetIP, 4888, dShapePackage);
//                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        mntmNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object[] options = {"Save", "Create new"};
                int n = JOptionPane.showOptionDialog(board,
                        "Would you like to save your current work?",
                        "Save your work first",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options,
                        options[1]);
                if (n == 0) {
                    saveFileAs();
                }
                canvas.whiteboard_info = new ArrayList<DShapeModel>();
                try {
                    registry = LocateRegistry.getRegistry(serverInetIP, 1099);
                    remoteAddress = (IjoinerAddresses) registry.lookup("joinerAddresses"); //从注册表中寻找joinerAddress method
                    addresses = remoteAddress.getAddressed();
                    send_update_whiteboard(0);
                } catch (NotBoundException | IOException ex) {
                    ex.printStackTrace();
                }

                canvas.setNull();
                repaint();

            }
        });

        mntmOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = JOptionPane.showInputDialog("File Name", null);
                if (result != null) {
                    File f = new File(result);
                    try {
                        open(f);
                    } catch (IOException | NotBoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        mntmOpenInSystem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File f = chooseOpenFile();
                try {
                    open(f);
                } catch (IOException | NotBoundException ex) {
                    ex.printStackTrace();
                }
            }
        });

        mntmSaveFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = JOptionPane.showInputDialog("File Name", null);
                if (result != null) {
                    File f = new File(result);
                    save(f);
                }
            }
        });

        mntmSaveFileAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveFileAs();
            }
        });

        mntmSaveImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = JOptionPane.showInputDialog("File Name", null);
                if (result != null) {
                    File f = new File(result);
                    saveImage(f);
                }
            }
        });

        mntmSaveIamgeAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File image = chooseSaveFile();
                saveImage(image);
            }
        });

        mntmDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (canvas.selected()) {
                    try {
                        canvas.removeShape();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    repaint();
                }
            }
        });

        mntmClearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.whiteboard_info = new ArrayList<>();
                try {
                    canvas.send_update_whiteboard(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                canvas.setNull();
                repaint();
            }
        });

        mntmMoveToFront.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.toFront();
                repaint();
            }
        });

        mntmMoveToBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.toBack();
                repaint();
            }
        });

//		mntmSetColor.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if(canvas.selected() ){
//					Color color = JColorChooser.showDialog(Whiteboard.this, "Set Color", canvas.getSelected().getColor());
//						System.out.println(color);
//					canvas.recolorShape(color);
//				}
//			}
//		});

        mntmAddLine.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Point p1 = new Point(25, 25);
                Point p2 = new Point(75, 75);
                DLineModel model = new DLineModel(p1, p2, penColor, Stroke);
                //model.setPoints(p1,p2);
                try {
                    canvas.addShape(model);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                canvas.repaint();
            }
        });

        mntmAddOval.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DOvalModel model = new DOvalModel(25, 25, 100, 100, penColor);
                try {
                    canvas.addShape(model);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                canvas.repaint();
            }
        });

        mntmAddRectangle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DRectModel model = new DRectModel(25, 25, 100, 100, penColor);
                try {
                    canvas.addShape(model);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                canvas.repaint();
            }
        });


        mntmAddText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = textField.getText();
                model = new DTextModel();
                model.setBounds(20, 20, 415, 80);
                try {
                    canvas.addShape(model);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                canvas.repaint();
            }
        });

        // Initial the canvas
        // Initial the canvas
        board.getContentPane().add(canvas);
        board.getContentPane().add(controlGroup, BorderLayout.NORTH);
        board.setSize(955, 692);
        board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        board.setVisible(true);
    }


    // Functions
    private void handleTextChange(DocumentEvent e) {
        if (canvas.selected() && canvas.getSelected() instanceof DText) {
            canvas.setText(textField.getText());
        }

    }

    public void updateTable(DShape selectedShape) {
        table.clearSelection();
        if (selectedShape != null) {
            if (freehand == false) {
                int index = tableModel.getRow(selectedShape.getModel());
//                System.out.println("location of the shape: " + index);
                table.setRowSelectionInterval(index, index);
            }
        }
    }

    public void add(DShape shape) {
        tableModel.add(shape.getModel());
    }

    public void delete(DShape shape) {
        tableModel.delete(shape.getModel());

    }

    public void deleteByIndex(int index) {
        tableModel.deleteByIndex(index);
    }

    public void updateModel(DShape shape, int index) {
        tableModel.updateModel(shape.getModel(), index);
    }

    public void toBack(DShape shape) {
        tableModel.toBack(shape.getModel());
    }

    public void toFront(DShape shape) {
        tableModel.toFront(shape.getModel());
    }

    public void clear() {
        tableModel.clear();
    }

    public void save(File file) {
        try {
            FileOutputStream saveFile = new FileOutputStream(file);
            ObjectOutputStream saveObjects = new ObjectOutputStream(saveFile);

            List<DShape> shapes = canvas.getShapes();
            DShapeModel[] models = new DShapeModel[shapes.size()];

            for (int i = 0; i < models.length; i++) {
                models[i] = shapes.get(i).getModel();
            }
            System.out.println(models);

            saveObjects.writeObject(models);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void open(File file) throws IOException, NotBoundException {
        try {
            FileInputStream openFile = new FileInputStream(file);
            ObjectInputStream openObjects = new ObjectInputStream(openFile);
            DShapeModel[] models = (DShapeModel[]) openObjects.readObject();
            openObjects.close();

            clear();
            canvas.whiteboard_info = new ArrayList<>();
            canvas.setNull();
            for (int i = 0; i < models.length; i++) {
                canvas.addShape(models[i]);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        registry = LocateRegistry.getRegistry(serverInetIP, 1099);
        remoteAddress = (IjoinerAddresses) registry.lookup("joinerAddresses"); //从注册表中寻找joinerAddress method
        addresses = remoteAddress.getAddressed();
        send_update_whiteboard(0);
    }

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

    public void saveImage(File file) {

        BufferedImage image = (BufferedImage) new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        canvas.paintAll(g2);

        try {
            javax.imageio.ImageIO.write(image, "PNG", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public File chooseSaveFile() {
        File file = null;
        JFileChooser fileChooser = new JFileChooser();
//		  fileChooser.setFileFilter(new FileNameExtensionFilter("*.png", "png"));
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        return file;
    }

    public File chooseOpenFile() {
        File file = null;
        JFileChooser fileChooser = new JFileChooser();
//		  fileChooser.setFileFilter(new FileNameExtensionFilter("*.png", "png"));
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        return file;
    }

    public void saveFileAs() {
        File f = chooseSaveFile();
        save(f);
    }

    public int getMode() {
        return mode;
    }


}
