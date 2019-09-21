package display;

import java.awt.*;
import java.util.*;

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
			rand.nextInt(displaySize.width - iconDvd.getIconWidth()),
			rand.nextInt(displaySize.height - iconDvd.getIconHeight())
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
			var vertHit = false;
			var displaySize = _paintHelper.displaySize;
			var iconDvd = _paintHelper.icons.Dvd;

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
				var xMax = displaySize.width - iconDvd.getIconWidth();
				if (position.x > xMax)
				{
					position.x = (int)xMax;
					isMovingUpNotDown = true;
					vertHit = true;
				}
			}
			
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
				var yMax = displaySize.height - iconDvd.getIconHeight();
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
}
