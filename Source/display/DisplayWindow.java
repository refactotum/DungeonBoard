package display;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import main.*;

import common.*;

public class DisplayWindow extends JFrame // todo - Should this inherit Display?
{
	private final Coords NullPos = new Coords(-100, -100);

	private PaintHelper _paintHelper = PaintHelper.Instance;

	private Coords mousePos;
	private CursorDirection handDirection;
	private DisplayTimer displayTimer;

	public DisplayWindow(Rectangle r)
	{
		super();

		setDefaults(r);
		createAndAddMouseMotionListener();
		createAndAddMouseListener();
	}
	
	private void setDefaults(Rectangle r)
	{
		setTitle("Display");
		setUndecorated(true);
		setIconImage(_paintHelper.icons.Program.getImage());
		setSize(r.getSize());
		setLocation(r.getLocation());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setCursor
		(
			getToolkit().createCustomCursor
			(
				_paintHelper.blankCursor.systemImage, new Coords(0, 0).toPoint(), "null"
			)
		);
		mousePos = NullPos;
		handDirection = CursorDirection.Up;
		displayTimer = new DisplayTimer(Coords.fromDimension(getSize()));
	}
	
	private void createAndAddMouseMotionListener()
	{
		addMouseMotionListener
		(
			new MouseMotionAdapter()
			{
				public void mouseDragged(MouseEvent e)
				{
					setMouse(Coords.fromPoint(e.getPoint()));
				}
				public void mouseMoved(MouseEvent e)
				{
					setMouse(Coords.fromPoint(e.getPoint()));
				}
			}
		);
	}
	
	private void createAndAddMouseListener()
	{
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
					setMouse(NullPos);
				}
				public void mouseEntered(MouseEvent e)
				{
					setMouse(Coords.fromPoint(e.getPoint()));
				}
			}
		);
	}

	private ImageIcon[] _iconsHands;
	private final int[] _iconsHandsOffsets = {-5, -100, -45, 0};
	private ImageIcon[] iconsHands()
	{
		var icons = _paintHelper.icons;
		if (this._iconsHands == null)
		{
			this._iconsHands = new ImageIcon[]
			{
				icons.load("hand0.png"),
				icons.load("hand1.png"),
				icons.load("hand2.png"),
				icons.load("hand3.png")
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
	
	public void setDisplay(Display displayNew, Display displayOld)
	{
		var thread = new Thread()
		{
			@Override
			public void run()
			{
				super.run();
				synchronized (displayOld)
				{
					remove(displayOld);
					displayOld.setIsMainDisplay(false);
					
					add(displayNew);
					validate();
					displayNew.setIsMainDisplay(true);
				}
			}
		};
		thread.start();
	}

	private void setMouse(Coords p)
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
