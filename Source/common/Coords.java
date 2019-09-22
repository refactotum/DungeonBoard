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
}
