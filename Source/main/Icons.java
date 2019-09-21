package main;

import javax.swing.*;

public class Icons
{
	public static Icons Instance = new Icons();

	public final ImageIcon Program = load("icon.gif");
	public final ImageIcon Refresh = load("refresh.gif");
	public final ImageIcon Flip = load("flip.gif");
	public final ImageIcon Settings = load("settings.gif");
	public final ImageIcon Dvd = load("dvdlogo.gif");
	public final ImageIcon Dvd2 = load("dvdlogo2.gif");

	public final ImageIcon[] PenDirectionLocks =
	{
		load("squigle.gif"),
		load("vertical.gif"),
		load("horizontal.gif")
	};

	public final ImageIcon[] TouchpadDrawModes =
	{
		load("mouse.gif"),
		load("visible.gif"),
		load("invisible.gif"),
		load("move.gif")
	};

	public final ImageIcon[] PenShapes =
	{
		load("circle.gif"),
		load("square.gif"),
		load("rect.gif")
	};

	public ImageIcon load(String resourceName)
	{
		return new ImageIcon(Icons.class.getResource("/resources/" + resourceName));
	}
}
