package display;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import main.*;

public class DisplayWindow extends JFrame // todo - Should this inherit Display?
{
	private final Point NULL_POS = new Point(-100, -100);

	private Settings _settings = Settings.Instance;
	private Settings.PaintHelper _paintHelper = _settings.paintHelper;

	private Point mousePos;
	private CursorDirection handDirection;
	private DisplayTimer displayTimer;

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
		
		setCursor
		(
			getToolkit().createCustomCursor
			(
				_paintHelper.blankCursor, new Point(0, 0), "null"
			)
		);
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
	
	private ImageIcon[] _iconsHands;
	private final int[] _iconsHandsOffsets = {-5, -100, -45, 0};
	private ImageIcon[] iconsHands()
	{
		if (this._iconsHands == null)
		{
			var iconLoader = _settings.icons;
			this._iconsHands = new ImageIcon[]
			{
				iconLoader.load("hand0.png"),
				iconLoader.load("hand1.png"),
				iconLoader.load("hand2.png"),
				iconLoader.load("hand3.png")
			};
		}
		return this._iconsHands;
	}

	public void paintDisplay(Graphics2D g2d)
	{
		var i = handDirection.ordinal();
		displayTimer.paint(g2d);
		g2d.drawImage
		(
			this.iconsHands()[i].getImage(),
			mousePos.x + _iconsHandsOffsets[i],
			mousePos.y + _iconsHandsOffsets[i == 0 ? 3 : i - 1],
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
