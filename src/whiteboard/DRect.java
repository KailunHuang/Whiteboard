package whiteboard;
import java.awt.*;

public class DRect extends DShape{
	
	
	public DRect(DShapeModel model) {
		super(model);
		
	}
	
	public DRectModel getModel() { 
        return (DRectModel) model; 
    } 

	public void draw(Graphics g, boolean selected){
		
		Rectangle bounds = model.getBounds();
		((Graphics2D)g).setStroke(new BasicStroke(model.getStroke()));
		g.setColor(model.getColor());
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		g.setColor(Color.BLACK);
		g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		if(selected) {
			drawKnobs(g); 
			
		}
		
	}

	@Override
	public void modelChanged(DShapeModel model) {
		// TODO Auto-generated method stub
		
	}
	
}
