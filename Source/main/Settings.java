package main;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class Settings
{
	public static Settings Instance = new Settings();
	private Main _main = Main.Instance;

	public final String applicationName = "Dungeon Board";

	public class Icons
	{
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
			return new ImageIcon(Settings.class.getResource("/resources/" + resourceName));
		}
	}
	public final Icons icons = new Icons();

	public class PaintHelper
	{
		public final BufferedImage blankCursor = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
		public BufferedImage paintImage;
		public BufferedImage paintControlImage;
		public Dimension displaySize;

		public final boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		public boolean[] paintImageS; // "S"?
		public final int sysThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		public final int paintGuideScale = 3;
		public final int paintPixelsPerMaskPixel = 5;
	}
	public final PaintHelper paintHelper = new PaintHelper();
}
