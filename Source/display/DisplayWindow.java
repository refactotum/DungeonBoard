package display;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import main.*;

public class DisplayWindow extends JFrame // todo - Should this inherit Display?
{
	private static final int[] HANDS_OFFSET = {-5, -100, -45, 0};
	private static final Point NULL_POS = new Point(-100, -100);

	private static final ImageIcon HANDS[] =
	{
		Settings.Instance.load("hand0.png"),
		Settings.Instance.load("hand1.png"),
		Settings.Instance.load("hand2.png"),
		Settings.Instance.load("hand3.png")
	};

	private Point mousePos;
	private CursorDirection handDirection;
	private DisplayTimer displayTimer;

	private Settings _settings = Settings.Instance;
	private Main _main = Main.Instance;

	public DisplayWindow(Rectangle r)
	{
		super();
		setTitle("Display");
		setUndecorated(true);
		setIconImage(_settings.icons.Program.getImage());
		setSize(r.getSize());
		setLocation(r.getLocation());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setCursor(getToolkit().createCustomCursor(
				_settings.BLANK_CURSOR, new Point(0, 0),
	            "null"));
		mousePos = NULL_POS;
		handDirection = CursorDirection.UP;
		displayTimer = new DisplayTimer(getSize());
		
		addMouseMotionListener
		(
			new MouseMotionAdapter()
			{
				public void mouseDragged(MouseEvent e)
				{
					setMouse(e.getPoint());
				}
				public void mouseMoved(MouseEvent e)
				{
					setMouse(e.getPoint());
				}
			}
		);
		
		addMouseListener
		(
			new MouseListener()
			{
				public void mouseReleased(MouseEvent e) {}
				public void mousePressed(MouseEvent e)
				{
					var cursorDirections = CursorDirection.values();
					var directionIndex = (handDirection.ordinal() + 1) % cursorDirections.length;
					handDirection = cursorDirections[directionIndex];
					repaint();
				}
				public void mouseClicked(MouseEvent e) {}
				public void mouseExited(MouseEvent e)
				{
					setMouse(NULL_POS);
				}
				public void mouseEntered(MouseEvent e)
				{
					setMouse(e.getPoint());
				}
			}
		);
	}
	
	public void paintDisplay(Graphics2D g2d)
	{
		var i = handDirection.ordinal();
		displayTimer.paint(g2d);
		g2d.drawImage
		(
			HANDS[i].getImage(),
			mousePos.x + HANDS_OFFSET[i],
			mousePos.y + HANDS_OFFSET[i == 0 ? 3 : i - 1],
			null
		);
	}
	
	public void setMode(Mode newMode, Mode oldMode)
	{
		var thread = new Thread()
		{
			@Override
			public void run()
			{
				super.run();
				synchronized (_main.getControl())
				{
					remove(_main.getDisplay(oldMode));
					_main.getDisplay(oldMode).setIsMainDisplay(false);
					add(_main.getDisplay(newMode));
					validate();
					_main.getDisplay(newMode).setIsMainDisplay(true);
				}
			}
		};
		thread.start();
	}
	
	private void setMouse(Point p)
	{
		mousePos = p;
		repaint();
	}

	public void setTimerSecondsRemaining(int seconds)
	{
		displayTimer.setTimerSecondsRemaining(seconds);
	}

	public void clearTimer()
	{
		displayTimer.clearTimer();
	}
}
