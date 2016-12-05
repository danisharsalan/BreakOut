import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class GameOverScreen extends Screen
{
  private int x, y;
  
  public GameOverScreen(GameState s, int w, int h) {
    super(s, w, h);
  }
  
  public void render(Graphics2D g) {
    
    g.setFont(new Font("Geneva", Font.BOLD, 42));
    g.setColor(Color.BLUE);
    g.drawString("Score: " + GameScreen.getScore() + " Press \"R\" to Play Breakout!", x, y);
  }
  
  public void update() {
        
    x++;
    y++;
    
    if(x > width)
      x = 0;
    
    if(y > height)
      y = 0;
  }
  
  public void keyPressed(KeyEvent e)
  {
    int code = e.getKeyCode();
    if(code == KeyEvent.VK_R){
      state.switchToGameScreen();
      GameScreen.setScore(0);
    }
  }
  
  public void keyReleased(KeyEvent e)
  {
  }
  
  public void mousePressed(Point2D p)
  {
  }
  public void mouseReleased(Point2D p)
  {
  }
  public void mouseMoved(Point2D p)
  {
  }
  public void mouseDragged(Point2D p)
  {
  }
}