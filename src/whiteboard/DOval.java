package whiteboard;
import java.awt.*;

public class DOval extends DShape {
	
	public DOval(DShapeModel model) {
		super(model);
		
	}

	public void draw(Graphics g, boolean selected){
		
		Rectangle bounds = model.getBounds(); 
		g.setColor(model.getColor());
		((Graphics2D)g).setStroke(new BasicStroke(model.getStroke()));
		g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
		g.setColor(Color.BLACK);
		g.drawOval(bounds.x, bounds.y, bounds.width, bounds.height);
		if(selected) drawKnobs(g); 
	}

	@Override
	public void modelChanged(DShapeModel model) {
		// TODO Auto-generated method stub
	}

	@Override
	public DOvalModel getModel() {
		return (DOvalModel) model; 
	}

}
