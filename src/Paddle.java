import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class Paddle extends Block
{

  public Paddle(int x, int y, int width, int height) {
    super(x, y, width, height, Color.WHITE);
  }

  public void setLocation(double x, double y) {
    getBounds().x = x;
    getBounds().y = y;
  }
}