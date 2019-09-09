package main;

import java.awt.*;

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

	public Dimension getSize()
	{
		return rectangle.getSize();
	}

	public Rectangle getRectangle()
	{
		return rectangle;
	}
}
