import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class Block
{
    private Rectangle2D.Double bounds;
    private Color color;

    public Block(int x, int y, int width, int height, Color c) {
        bounds = new Rectangle2D.Double(x, y, width, height);
        color = c;
    }
    
    //Returns true if the block intersects the Sphere
    public boolean intersects(Sphere sph) {
    
        return sph.getBounds().intersects(bounds.x, bounds.y, bounds.width, bounds.height);
    
    }
    
    public Rectangle2D.Double getBounds() {
    	return bounds;
    }
    
    public void draw(Graphics2D g)
    {
    	g.setColor(color);
    	g.fill(bounds);
    }
}