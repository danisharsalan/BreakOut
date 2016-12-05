import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

public class GameScreen extends Screen
{
  private ArrayList<Block> boundaries;
  private ArrayList<Block> blocks;
  private Paddle playerPaddle;
  private Sphere ball;
  private static int score = 0;

  private boolean left, right;
  private int updateCounter = 0;	

  public GameScreen(GameState s, int w, int h) {
    super(s, w, h);

    playerPaddle = new Paddle(w/2 - 25, h - 20, 100, 20);
    ball = new Sphere(w/2 - 10, h/2 - 10, 10);
    blocks = new ArrayList<Block>();
    boundaries = new ArrayList<Block>();
    int rowNum = 5;
    int colNum = 9;

    for(int i = 0; i < rowNum; i++){
      for(int a = 0; a < colNum; a++){
        Random randomGenerator = new Random();
        int red = randomGenerator.nextInt(256);
        int green = randomGenerator.nextInt(256);
        int blue = randomGenerator.nextInt(256);
        Color randomColor = new Color(red,green,blue);
        blocks.add(new Block(a*80 + 50 , i*40 + 50, 70, 30, randomColor));
      }
    }

    boundaries.add(new Block(0, 0, 10, h, Color.GREEN)); //left
    boundaries.add(new Block(0, 0, w, 10, Color.GREEN)); //top
    boundaries.add(new Block(w-10, 0, 10, h, Color.GREEN)); //right
  }

  public void render(Graphics2D g) {

    ball.draw(g);

    for(Block b: boundaries)
      b.draw(g);

    for(Block b: blocks)
      b.draw(g);

    playerPaddle.draw(g);

    g.setColor(Color.WHITE);
	g.drawString(("Score: " + score), 20, height - 14);
  }
  
  public static int getScore(){
	  return score;
  }
  
  public static void setScore(int s){
    score = s;
  }

  public void update() {

    updateCounter++;

    //move the ball
    ball.update();


    //move player two based on the key presses
    if(right)
      playerPaddle.setLocation(playerPaddle.getBounds().x+5, playerPaddle.getBounds().y);
    if(left)
      playerPaddle.setLocation(playerPaddle.getBounds().x-5, playerPaddle.getBounds().y);
    
    if(playerPaddle.intersects(ball)){
    	ball.switchYVel();
    }
    for(int f = 0; f < boundaries.size(); f++){
    	if(f == 1 && boundaries.get(f).intersects(ball)){
    		ball.switchYVel();
    	} else if((f == 0 || f == 2) && boundaries.get(f).intersects(ball)){
    		ball.switchXVel();
    	}
    }
    if(blocks.size() > 0){
    	for(int g = 0; g < blocks.size(); g++){
    		if(blocks.get(g).intersects(ball)){
    			ball.switchYVel();
    			blocks.remove(g);
    			score++;
    		}
    	}
    } else {
    	state.switchToGameOverScreen();
    }
    
    if(ball.getYLocation() > GameRunner.getDesiredHeight()-15){
    	ball.setLocation(GameRunner.getDesiredWidth()/2 - 10, GameRunner.getDesiredHeight()/2 - 10);
    	ball.randSetXVel();
    }
    
    if(playerPaddle.getBounds().x < boundaries.get(0).getBounds().x+10){
    	playerPaddle.setLocation(10, playerPaddle.getBounds().y);
    }
    
    if(playerPaddle.getBounds().x > boundaries.get(2).getBounds().x-100){
    	playerPaddle.setLocation(690, playerPaddle.getBounds().y);
    }
  }

//move player two based on the keyboard
  public void keyPressed(KeyEvent e)
  {
    int code = e.getKeyCode();
    if(code == KeyEvent.VK_Q)
      state.switchToWelcomeScreen();

    if(code == KeyEvent.VK_LEFT)
      left = true;
    if(code == KeyEvent.VK_RIGHT)
      right = true;
  }

  public void keyReleased(KeyEvent e)
  {
    int code = e.getKeyCode();
    if(code == KeyEvent.VK_LEFT)
      left = false;
    if(code == KeyEvent.VK_RIGHT)
      right = false;
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