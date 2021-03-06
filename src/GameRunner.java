import javax.swing.*;

import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.awt.geom.Point2D;
import java.util.concurrent.*;

public class GameRunner extends JFrame implements WindowListener, KeyListener, MouseListener, MouseMotionListener, Runnable
{		
	private GraphicsDevice gd = null; //Needed for fullscreen mode.
	private BufferStrategy strategy = null; // Needed for double buffering.
	
	private Thread thread;
	private boolean shouldStop;
	
	private int fps = 80;
	private long period;
	private double effectiveFPS;
	private double effectiveUPS;
			
	private static int desiredWidth = 800;
	private static int desiredHeight = 600;
	private AffineTransform screenScale;
	private AffineTransform inverseScale = new AffineTransform();
	
	private boolean shouldDoFullScreen = false;
	private boolean shouldScaleScreen = false;
	
	private ConcurrentLinkedQueue<KeyEvent> queuedKeyEvents = new ConcurrentLinkedQueue<KeyEvent>();
	private ConcurrentLinkedQueue<MouseEvent> queuedMouseEvents = new ConcurrentLinkedQueue<MouseEvent>();
	private ConcurrentLinkedQueue<Integer> queuedFPSChanges = new ConcurrentLinkedQueue<Integer>();
	
	private GameState state;
	
	public static void main(String args[]) 
	{
		new GameRunner();
	}
	
	public static int getDesiredWidth(){
		return desiredWidth;
	}
	
	public static int getDesiredHeight(){
		return desiredHeight;
	}
	
	public GameRunner() {
		
		super(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
		setIgnoreRepaint(true);
		
		try {
			init();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			exit();
		}
	}
	
	/**
	 * Called by the constructors, this initializes fields and
	 * sets up the Graphics environment for display.
	 * @throws Exception
	 */
	private void init() throws Exception {
		//Set window properties
		setFocusTraversalKeysEnabled(false);		
		setResizable(false);
		setIgnoreRepaint(true);
		setUndecorated(true);
		
		gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		//Set fullscreen mode
		if(shouldDoFullScreen)
		{
			if (!gd.isFullScreenSupported())
				throw new Exception("Fullscreen is not supported");
			gd.setFullScreenWindow(this);
		}
		
		//hide the cursor
		setCursor(getToolkit().createCustomCursor(
	            new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),
	            "null"));
		
		//change screen dimensions and do scaling if necessary
		if(shouldScaleScreen && shouldDoFullScreen)
			pickBestDisplayMode();
		
		if(shouldDoFullScreen)
		{
			//set the diminsions of the window
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			setSize(d);
			
			//if we're not scaling, change our desired width and height to match the actual screen
			if(!shouldScaleScreen)
			{
				desiredWidth = d.width;
				desiredHeight = d.height;
			}
		}
		else {
			
			//if not full screen, set the size of the frame as desired, and center
			setSize(new Dimension(desiredWidth, desiredHeight));
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((d.width - desiredWidth)/2, (d.height - desiredHeight)/2);
		}

		
		setLayout(null);
		setFocusable(true);
		requestFocus();
		
		setVisible(true);
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		
		state = new GameState(desiredWidth, desiredHeight);
		
		//Add action all of the listeners
		addKeyListener(this);
		addWindowListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
		//this delay seems to make things look better on start!
		paintScreen();
		javax.swing.Timer timer = new javax.swing.Timer(10, new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				createThread();
				startThread();
			}});
		timer.setRepeats(false);
		timer.start();
	}
	
	
	//try to switch resolutions - scale the screen if ncessary
	private void pickBestDisplayMode() {
		DisplayMode oldMode = gd.getDisplayMode();
		
		try
		{
			gd.setDisplayMode(new DisplayMode(desiredWidth, desiredHeight, 32, DisplayMode.REFRESH_RATE_UNKNOWN));
		}
		catch(Exception e)
		{
			gd.setDisplayMode(oldMode);
			
			int currentWidth = oldMode.getWidth();
			int currentHeight = oldMode.getHeight();
			
			double widthRatio = currentWidth / (double)desiredWidth;
			double heightRatio = currentHeight / (double)desiredHeight;
			
			double scaleFactor = 1.0;
			
			double xTrans = 0;
			double yTrans = 0;
			
			if(desiredWidth <= currentWidth && desiredHeight <= currentHeight)
			{
				scaleFactor = 1.0;
			}
			else if(desiredWidth >= currentWidth && desiredHeight >= currentHeight) {
				scaleFactor = Math.min(widthRatio, heightRatio);
			}
			else if(desiredWidth >= currentWidth) {
				scaleFactor = widthRatio;
			}
			else //desiredHeight > height
			{
				scaleFactor = heightRatio;
			}
			
			
			xTrans = (currentWidth - desiredWidth*scaleFactor)/(2 * scaleFactor);
			yTrans = (currentHeight - desiredHeight*scaleFactor)/(2 * scaleFactor);
			screenScale = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
			screenScale.translate(xTrans, yTrans);
			
			try {
				inverseScale = screenScale.createInverse();
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}		
	}
	
	//not thread safe!
	public void setFPS(int f)
	{
		queuedFPSChanges.add(f);
	}
	
	
	//event methods
	public void windowClosing(WindowEvent e) {
		exit();
	}
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void keyTyped(KeyEvent e) {}
	public void mouseClicked(MouseEvent e){}
	
	public void mouseDragged(MouseEvent e)
	{		
		queuedMouseEvents.add(e);
	}
	
	public void mouseMoved(MouseEvent e)
	{		
		queuedMouseEvents.add(e);
	}
	
	public void mouseReleased(MouseEvent e)
	{		
		queuedMouseEvents.add(e);
	}
	
	public void mousePressed(MouseEvent e)
	{		
		queuedMouseEvents.add(e);
	}
	
	//quits game when escape is pressed
	public void keyPressed(KeyEvent e) 
	{	
		int code = e.getKeyCode();
		if(code == KeyEvent.VK_ESCAPE)
			exit();
		
		queuedKeyEvents.add(e);
	}
	
	public void keyReleased(KeyEvent e)
	{
		queuedKeyEvents.add(e);
	}
	
	
	public void render(Graphics g)
	{
		Screen screen = state.currentActiveScreen();
		screen.render((Graphics2D)g);
	}
	
	public void update()
	{
		Screen screen = state.currentActiveScreen();
		screen.update();
	}
	
	public void exit() {
		stopThread();
		if (gd != null)
			gd.setFullScreenWindow(null);
		dispose();
		System.exit(0);
	}
	
	//run thread
	public void run()
	{
		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;
		
		period = (long)((1000.0/fps) * 1000000L);
		
		final int NO_DELAYS_PER_YIELD = 16;
		final int MAX_FRAME_SKIPS = 5;
		
		beforeTime = System.nanoTime();
		
		long time = System.nanoTime();
		long frameCounter = 0;
		long updateCounter = 0;
		
		while(!shouldStop)
		{
			update();
			updateCounter++;
			
			paintScreen();
			
			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;
			sleepTime = (period - timeDiff) - overSleepTime;
			
			if(sleepTime > 0)
			{
				try
				{	
					TimeUnit.NANOSECONDS.sleep(sleepTime);
				}
				catch(Exception e)
				{
				}
				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
			}
			else
			{				
				excess -= sleepTime;
				overSleepTime = 0L;
				
				noDelays++;
				if(noDelays >= NO_DELAYS_PER_YIELD)
				{
					Thread.yield();
					noDelays = 0;
				}
			}
			
			int skips = 0;
			while(excess > period && skips < MAX_FRAME_SKIPS)
			{
				excess -= period;
				update();
				updateCounter++;
				
				skips++;
			}
			
			frameCounter++;
			beforeTime = System.nanoTime();
			if(beforeTime - time > 1000000000L) //1 second has passed
			{
				effectiveFPS = (double)frameCounter * 1000000000L / (beforeTime - time);
				effectiveUPS = (double)updateCounter * 1000000000L / (beforeTime - time);
				
				frameCounter = 0;
				updateCounter = 0;
				time = beforeTime;
			}	
			
			//check for fps changes
			while(!queuedFPSChanges.isEmpty())
				changeFPS(queuedFPSChanges.remove());
			
			//check for key events
			while(!queuedKeyEvents.isEmpty())
				doKey(queuedKeyEvents.remove());
			
			//check for mouse events
			while(!queuedMouseEvents.isEmpty())
				doMouse(queuedMouseEvents.remove());
		}
		
		thread = null;
	}
	
	//updates the FPS
	private void changeFPS(int newFPS) {
		fps = newFPS;
		period = (long)((1000.0/fps) * 1000000L);
	}
	
	
	//tells the current screen about a key event
	private void doKey(KeyEvent e) {
		
		Screen screen = state.currentActiveScreen();
		
		if(e.getID() == KeyEvent.KEY_PRESSED)
			screen.keyPressed(e);
		else if(e.getID() == KeyEvent.KEY_RELEASED)
			screen.keyReleased(e);
		
	}
	
	//tells the current screen about a mouse event
	private void doMouse(MouseEvent e) {
		
		Point2D p = inverseScale.transform(e.getPoint(), null);
		Screen screen = state.currentActiveScreen();
		
		if(e.getID() == MouseEvent.MOUSE_PRESSED)
			screen.mousePressed(p);
		else if(e.getID() == MouseEvent.MOUSE_RELEASED)
			screen.mouseReleased(p);
		else if(e.getID() == MouseEvent.MOUSE_DRAGGED)
			screen.mouseDragged(p);
		else if(e.getID() == MouseEvent.MOUSE_MOVED)
			screen.mouseMoved(p);
	}
	
	/*DRAWS TO THE SCREEN - HANDLE TRANSFORMATIONS*/
	private void paintScreen()
	{
		if(strategy == null)
			return;
		
		Graphics g = strategy.getDrawGraphics();
		Graphics2D g2 = ((Graphics2D)g);
		
		
		
		if(screenScale != null)
		{			
			//pretty gradient fill on the edges!
			int dist;
			dist = (int)(screenScale.getTranslateX() / screenScale.getScaleX());
		
			Paint p = g2.getPaint();
			GradientPaint whiteToBlack = new GradientPaint(0, 0, Color.DARK_GRAY, dist, 0, Color.BLACK);
			g2.setPaint(whiteToBlack);
			g2.fillRect(0, 0, dist, getHeight());
			
			GradientPaint blackToWhite = new GradientPaint(getWidth() - dist, 0, Color.BLACK, getWidth(), 0, Color.DARK_GRAY);
			g2.setPaint(blackToWhite);
			g2.fillRect(getWidth() - dist, 0, dist, getHeight());
			g2.setPaint(p);
			//end gradient stuff
		}
		
		
		//rendering hints to speed drawing up
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		
		//do scaling and cliping
		if(screenScale != null) {
			((Graphics2D)g).transform(screenScale);
			g.setClip(0, 0, desiredWidth, desiredHeight);
		}
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, desiredWidth, desiredHeight);
		render(g);
		
		
		strategy.show();
	}
	
	
	/*THREAD SPECIFIC METHODS*/
	private synchronized void createThread()
	{
		if(thread == null)
		{
			thread = new Thread(this);
			shouldStop = true;
		}
	}
	
	private synchronized void startThread()
	{
		if(thread != null && shouldStop == true)
		{
			shouldStop = false;
			thread.start();
		}
	}
	
	private synchronized void stopThread()
	{
		shouldStop = true;
	}
	
	public synchronized boolean isRunning()
	{
		return thread != null && shouldStop == false;
	}
}
