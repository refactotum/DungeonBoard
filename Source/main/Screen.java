package main;

import java.awt.*;

import common.*;

public class Screen
{
	private final Rectangle rectangle;
	private final String name;

	public Screen(GraphicsDevice graphicsDevice)
	{
		rectangle = graphicsDevice.getDefaultConfiguration().getBounds();
		name = graphicsDevice.getIDstring();
	}
	
	@Override
	public String toString()
	{
		return name + "  " + rectangle.width + "x" + rectangle.height;
	}

	public Coords getSize()
	{
		return Coords.fromDimension(rectangle.getSize());
	}

	public Rectangle getRectangle()
	{
		return rectangle;
	}
}
