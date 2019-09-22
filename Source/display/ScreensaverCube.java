package display;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import common.*;
import main.*;

public class ScreensaverCube
{
	// An image that floats around the screen like the old DvD logo would on DvD players.

	private final int pixelsPerMove;
	private Point position;

	private boolean isMovingUpNotDown;
	private boolean isMovingLeftNotRight;
	private boolean isInCorner;

	private PaintHelper _paintHelper = PaintHelper.Instance;

	public ScreensaverCube()
	{
		var rand = new Random();
		pixelsPerMove = rand.nextInt(5) + 1;
		var displaySize = _paintHelper.displaySize;
		var iconDvd = _paintHelper.icons.Dvd;
		position = new Point
		(
			rand.nextInt(displaySize.x - iconDvd.getIconWidth()),
			rand.nextInt(displaySize.y - iconDvd.getIconHeight())
		);
		isMovingUpNotDown = rand.nextBoolean();
		isMovingLeftNotRight = rand.nextBoolean();
		isInCorner = false;
	}
	
	public void paint(Graphics2D g2d)
	{
		var icons = _paintHelper.icons;
		var iconToUse = (isInCorner ? icons.Dvd2 : icons.Dvd);
		g2d.drawImage(iconToUse.getImage(), position.x, position.y, null);
	}
	
	public void move()
	{
		if (isInCorner == false)
		{
			var displaySize = _paintHelper.displaySize;
			var iconDvd = _paintHelper.icons.Dvd;

			var vertHit = moveVertical(displaySize, iconDvd);
			moveHorizontal(displaySize, iconDvd, vertHit);
		}
	}
	
	private boolean moveVertical(Coords displaySize, ImageIcon iconDvd)
	{
		var vertHit = false;

		if (isMovingUpNotDown)
		{
			position.x -= pixelsPerMove;
			if (position.x < 0)
			{
				position.x = 0;
				isMovingUpNotDown = false;
				vertHit = true;
			}
		}
		else
		{
			position.x += pixelsPerMove;
			var xMax = displaySize.x - iconDvd.getIconWidth();
			if (position.x > xMax)
			{
				position.x = (int)xMax;
				isMovingUpNotDown = true;
				vertHit = true;
			}
		}
		
		return vertHit;
	}
	
	private void moveHorizontal(Coords displaySize, ImageIcon iconDvd, boolean vertHit)
	{
		if (isMovingLeftNotRight)
		{
			position.y -= pixelsPerMove;
			if (position.y < 0)
			{
				position.y = 0;
				isMovingLeftNotRight = false;
				if (vertHit)
				{
					isInCorner = true;
				}
			}
		}
		else
		{
			position.y += pixelsPerMove;
			var yMax = displaySize.x - iconDvd.getIconHeight();
			if (position.y > yMax)
			{
				position.y = (int)yMax;
				isMovingLeftNotRight = true;
				if (vertHit)
				{
					isInCorner = true;
				}
			}
		}
	}
}
