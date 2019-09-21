package main;

import java.awt.*;
import java.awt.image.*;

public class PaintHelper
{
	public static PaintHelper Instance = new PaintHelper();

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
