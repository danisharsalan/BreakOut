import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class Sphere
{
    private Ellipse2D.Double bounds;
    private double xVel;
    private double yVel;
    
    public Sphere(double x, double y, double radius) {
        bounds = new Ellipse2D.Double(x, y, 2*radius, 2*radius);
        xVel = 2;
        yVel = 2;
    }
        
    public void setLocation(double x, double y) {
        bounds.x = x;
        bounds.y = y;
    }
    
    public double getXLocation(){
    	return bounds.x;
    }
    
    public double getYLocation(){
    	return bounds.y;
    }
    
    public Ellipse2D.Double getBounds() {
        return bounds;
    }
    
    public void draw(Graphics2D g)
    {
      Random randomGenerator = new Random();
      int red = randomGenerator.nextInt(256);
      int green = randomGenerator.nextInt(256);
      int blue = randomGenerator.nextInt(256);
      Color randomColor = new Color(red,green,blue);
    	g.setColor(randomColor);
    	g.fillOval((int)bounds.getX(), (int)bounds.getY(), (int)bounds.getWidth(), (int)bounds.getHeight());
    }
    
    public void switchYVel(){
    	yVel *= -1;
    }
    
    public void switchXVel(){
    	xVel *= -1;
    }
    
    public void randSetXVel(){
    	if(Math.random() > 0.5){
    		xVel = 2;
    	} else {
    		xVel = -2;
    	}
    }
    
    public void update()
    {
    	bounds.x += xVel;
    	bounds.y += yVel;
    }
}