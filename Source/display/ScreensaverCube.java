package display;

import java.awt.*;
import java.util.*;

import main.*;

public class ScreensaverCube
{
	// An image that floats around the screen like the old DvD logo would on DvD players.

	private final int PIXELS_PER_MOVE;
	private Point position;

	private boolean isMovingUpNotDown;
	private boolean isMovingLeftNotRight;
	private boolean isInCorner;

	private Settings _settings;

	public ScreensaverCube(Settings settings)
	{
		_settings = settings;
		var rand = new Random();
		PIXELS_PER_MOVE = rand.nextInt(5) + 1;
		var displaySize = _settings.DISPLAY_SIZE;
		var iconDvd = _settings.icons.Dvd;
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
		if (isInCorner)
		{
			g2d.drawImage(_settings.icons.Dvd2.getImage(), position.x, position.y, null);
		}
		else
		{
			g2d.drawImage(_settings.icons.Dvd.getImage(), position.x, position.y, null);
		}
	}
	
	public void move()
	{
		if (isInCorner == false)
		{
			var vertHit = false;
			var displaySize = _settings.DISPLAY_SIZE;
			var iconDvd = _settings.icons.Dvd;

			if (isMovingUpNotDown)
			{
				position.x -= PIXELS_PER_MOVE;
				if (position.x < 0)
				{
					position.x = 0;
					isMovingUpNotDown = false;
					vertHit = true;
				}
			}
			else
			{
				position.x += PIXELS_PER_MOVE;
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
				position.y -= PIXELS_PER_MOVE;
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
				position.y += PIXELS_PER_MOVE;
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
