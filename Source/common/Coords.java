package common;

import java.awt.*;

public class Coords
{
	public int x;
	public int y;

	public Coords(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public static Coords fromDimension(Dimension dimension)
	{
		return new Coords(dimension.width, dimension.height);
	}

	public Dimension toDimension()
	{
		return new Dimension(this.x, this.y);
	}

	public static Coords fromPoint(Point point)
	{
		return new Coords(point.x, point.y);
	}

	public Point toPoint()
	{
		return new Point(this.x, this.y);
	}
}
