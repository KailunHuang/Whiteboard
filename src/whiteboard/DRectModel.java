package whiteboard;
import java.awt.Color;
import java.io.Serializable;

public class DRectModel extends DShapeModel implements Serializable{

	public DRectModel(int x, int y, int width, int height, Color color){
		super(x, y, width, height, color);
	} 

}
