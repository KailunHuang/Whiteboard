	package whiteboard;
	
		
	import CreateWhiteBoard.IjoinerAddresses;
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
	import java.rmi.NotBoundException;
	import java.rmi.registry.LocateRegistry;
	import java.rmi.registry.Registry;
	import java.util.*;

	import javax.swing.JPanel;
	
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
		private static Hashtable<Integer, DShapeModel> whiteboard_info = new Hashtable<>();
		
		public Canvas(Whiteboard board) throws ClassNotFoundException {
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
		}
		
		public void canvasClicked(){	
			addMouseListener( new MouseAdapter() {
	            public void mousePressed(MouseEvent e) {
	            	
	            	if(board.freehand == true || board.eraser == true) {
	            		x_start = e.getX();
	            		y_start = e.getY();
	            	}
	            	
	            	if(board.getMode() != 2){
	            	Point pt = e.getPoint();
	            	x = e.getX(); 
	                y = e.getY(); 
	                movingKnob = null; 
	                pivotKnob = null; 
	                
	                if(selected != null) { 
	                	getSelection(pt);
	                }
	                
	                if(movingKnob == null) { 
	                    selected = null; 
	                    for(int i = 0; i < shapes.size(); i++){
	                    	if(shapes.get(i).containsPoint(pt)) 
	                            selected = shapes.get(i); 
	                    }  
	                    
	                }
	                
	                repaint(); 
	                
	            	}     
	            	
	            }
	            
	        });           
			
		}

		public void drag(){
			addMouseMotionListener( new MouseMotionAdapter() {
	            public void mouseDragged(MouseEvent e) {
	            	
	            	if(board.freehand == true) {
	            		x_drag = e.getX();
	                    y_drag = e.getY();
	                    p_start = new Point(x_start, y_start);
	                    p_drag = new Point(x_drag, y_drag);
	                    DLineModel model = new DLineModel(p_start, p_drag, board.penColor);
//	                    System.out.println(board.freehandColor);
	                    // Set Pen Color
	                    model.setColor(board.penColor);
	                    model.setStroke(board.Stroke);
	    				addShape(model);
	    				repaint();
	    				x_start = x_drag;
	    	            y_start = y_drag;
	    	            
	            	}else if (board.eraser == true) {
	            		x_drag = e.getX();
	                    y_drag = e.getY();
	                    p_start = new Point(x_start, y_start);
	                    p_drag = new Point(x_drag, y_drag);
	                    DLineModel model = new DLineModel(p_start, p_drag);
//	                    System.out.println(board.freehandColor);
	                    // Set Pen Color
	                    model.setColor(Color.WHITE);
	                    model.setStroke(board.Stroke);
	    				addShape(model);
	    				repaint();
	    				x_start = x_drag;
	    	            y_start = y_drag;
	            		
	            	}else if(board.getMode() != 2){
	            		int dx = e.getX()-x;
	            		int dy = e.getY()-y;
	            		x = e.getX();
	            		y = e.getY();
	            	
	            		if(selected != null){
	            			selected.moveBy(dx, dy);
	            			board.updateTable(selected);
	            			//move
	            			repaint();
	            		}
	            	
	            		if(movingKnob != null){
	            			movingKnob.x += dx;
	            			movingKnob.y += dy;
	            			selected.resize(pivotKnob,movingKnob);
	            		
	            		}
	            	
	            	}
	            	
	            }
	      });
			addMouseListener( new MouseAdapter() {
	            public void mouseReleased(MouseEvent e) {
	            	if (board.freehand == true) {
	            		board.freehand = false;
	            		selected = null;
	            		System.out.println("Relased the mouse!");
	            		System.out.println("Freehand model: "+ board.freehand);
	            	}else if (board.eraser == true) {
	            		board.eraser = false;
	            		selected = null;
	            		System.out.println("Relased the mouse!");
	            		System.out.println("Freehand model: "+ board.freehand);
	            	}
	            }
			});
	   }
		
		public void getSelection(Point pt){
			knobs = selected.getKnobs();
			for(int i = 0; i < knobs.size(); i++){
				Rectangle knob = new Rectangle(knobs.get(i).x - 4, knobs.get(i).y - 4, 9, 9);
	        	if(knob.contains(pt)){
	        		int j = 0;
	        		movingKnob = new Point(knobs.get(i));
	        		if(knobs.size() == 2){
	        			if(i == 0) 
	        				j = 1;
	        			else if(i == 1) 
	        				j = 0; 
	        		}
	        		else {
	        			if(i == 0) 
	        				j = 3;
	        			else if(i == 1) 
	        				j = 2; 
	        			else if(i == 2) 
	        				j = 1; 
	        			else if(i == 3) 
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
	  
	        for(int i = 0; i < shapes.size(); i++){
	        	DShape shape = shapes.get(i);
	        	shape.draw(g, (selected == shape));
	        }
	        
	    }
		
		public void addShape(DShapeModel model) {
//			System.out.println(model);
			model.setStroke(board.Stroke);
			System.out.println(board.Stroke);
			if(board.getMode() != 2){
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
			//发送新建的shape
			repaint();
			}
		}
		
		public void removeShape() { 
	        
			if(selected()) {
			shapes.remove(selected); 
			board.delete(selected);
			selected = null;
			//发送删除
			repaint();
			}
		} 

//		overriding removeShape
		public void removeShape(DShape shape){
			shapes.remove(shape);
			board.delete(shape);

			repaint();
		}

		public void updateShape(DShape shape, int index){
			shapes.set(index, shape);
			board.updateModel(shape, index);

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
		
		public List<DShape> getShapes()
		{
			return shapes;
		}
		
		public void setText(String text) { 
		    if(selected()) {
		        ((DText)selected).setText(text);
		        //发送修改text
		        repaint();
		    }
		    
		} 
		
		public void setFont(String fontName) { 
		    if(selected()) {
		        ((DText)selected).setFont(fontName);
		        //发送修改font
		        repaint();
		    }
		} 
		
		public void setNull(){
			shapes.clear();
			board.clear();
			selected = null;
		    repaint();
		}
			
		public boolean selected(){
			if (selected != null) 
				return true;
			else 
				return false;
		}

		public void sendEditShape(){

		}

		public void sendDeleteShape(){

		}

		public void sendAddShape(){

		}

		//-----------THREAD CLASS---------------------//

		static class createThread extends Thread {
			private int port;
			private String InetIP;
			private Canvas canvas;

			public createThread(String InetIP, int port, Canvas canvas) {
				this.port = port;
				this.InetIP = InetIP;
				this.canvas = canvas;
			}

			public synchronized void run() {
				try {
					registry = LocateRegistry.getRegistry(InetIP, 1099);
					remoteAddress = (IjoinerAddresses) registry.lookup("joinerAddresses"); //从注册表中寻找joinerAddress method
					while (true) {
						String str = UDPReceive.receive(port);
						System.out.println("收到了信息：" + str);
						whiteboard_info = remoteAddress.get_whiteBoard_Info();
						int modified_index = Integer.parseInt(str.substring(2));
						if (modified_index == 0) { // 这里是单纯的添加了新的图形
							addShapesFromHashTable(whiteboard_info);
						} else if (modified_index < 0){ //这里将arraylist中的图形给删掉
							int true_index = Math.abs(modified_index);
							deletShapesFromHashTable(true_index);
						}
						else { //修改某一个图形
							editShapeFromHashTable(whiteboard_info, modified_index);
						}
					}
				} catch (IOException | NotBoundException e) {
					e.printStackTrace();
				}
			}

			public void addShapesFromHashTable(Hashtable<Integer, DShapeModel> hashtable){
				int arraySize = canvas.shapes.size();
				int htSize = hashtable.size();
				if (htSize > arraySize){
					for (int i = arraySize; i < htSize; i++){
						if (hashtable.get(i)==null){
							continue;
						}
						canvas.addShape(hashtable.get(i));
					}
				}
			}

			public void deletShapesFromHashTable(int index){
				int arraySize = canvas.shapes.size();
				if (index < arraySize){
					DShape shape =  canvas.shapes.get(index);
					canvas.removeShape(shape);
				}
			}

			public void editShapeFromHashTable(Hashtable<Integer, DShapeModel> hashtable, int index){
				DShapeModel model = hashtable.get(index);
				DShape shape = buildShapeByModel(model);
				canvas.updateShape(shape, index);
			}

			public DShape buildShapeByModel(DShapeModel model){
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
		
		
		
