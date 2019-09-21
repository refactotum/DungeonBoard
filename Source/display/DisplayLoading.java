package display;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import main.*;

public class DisplayLoading extends Display
{
	private static final int ticksPerSecond = 20;
	private static final int millisecondsPerTick = 1000 / ticksPerSecond;
	private static final int ticksPerFade = 20;

	private int ticksPerImage = 400;
	private LinkedList<ScreensaverCube> screensaverCubes;
	private LinkedList<String> fileNamesNotYetShown;
	private BufferedImage oldImage;
	private BufferedImage currentImage;
	private Thread paintThread;
	private boolean isMainDisplay;
	private boolean shouldImagesBeUpscaled;
	private short ticksSinceImageChanged;
	private float fadeOpacity;

	public DisplayLoading()
	{
		super();
		screensaverCubes = new LinkedList<>();
		paintThread = new Thread();
		fileNamesNotYetShown = new LinkedList<>();
		shouldImagesBeUpscaled = false;
		ticksSinceImageChanged = ticksPerFade;
		fadeOpacity = 1;
		changeToNextImage();
		setVisible(true);
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		var g2d = (Graphics2D) g;
		if (currentImage != null)
		{
			var displaySize = _paintHelper.displaySize;
			
			if (shouldImagesBeUpscaled)
			{
				if (ticksSinceImageChanged <= ticksPerFade)
				{
					g2d.drawImage(oldImage, 0, 0, displaySize.width, displaySize.height, null);
				}
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeOpacity));
				g2d.drawImage(currentImage, 0, 0, displaySize.width, displaySize.height, null);
			}
			else
			{
				g2d.setColor(new Color(currentImage.getRGB(0, 0)));
				g2d.fillRect(0, 0, displaySize.width, displaySize.height);

				if (ticksSinceImageChanged <= ticksPerFade && oldImage != null)
				{
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - fadeOpacity));
					g2d.drawImage
					(
						oldImage,
						(displaySize.width - oldImage.getWidth()) / 2,
						(displaySize.height - oldImage.getHeight()) / 2,
						null
					);
				}
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeOpacity));
				g2d.drawImage
				(
					currentImage, 
					(displaySize.width - currentImage.getWidth()) / 2,
					(displaySize.height - currentImage.getHeight()) / 2,
					null
				);
			}
		}
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
		for (var c: screensaverCubes)
		{
			c.paint(g2d);
		}
		paintMouse(g2d);
		g2d.dispose();
	}
	
	@Override
	public void setIsMainDisplay(boolean value)
	{
		if (value == true)
		{
			restart(false);
			repaint();
		}
		isMainDisplay = value;
	}
	
	public void setSecondsPerImage(int value)
	{
		ticksPerImage = value * ticksPerSecond;
	}

	public void setShouldImagesBeUpscaled(boolean value)
	{
		shouldImagesBeUpscaled = value;
		repaint();
	}
	
	public void addScreensaverCube()
	{
		screensaverCubes.add(new ScreensaverCube());
		repaint();
	}

	public void clearScreensaverCubes()
	{
		synchronized (screensaverCubes)
		{
			screensaverCubes.clear();
		}
	}

	private void motion()
	{
		ticksSinceImageChanged++;
		repaint();
		if (ticksSinceImageChanged <= ticksPerFade)
		{
			fadeOpacity = (float)ticksSinceImageChanged / ticksPerFade;
		}
		else if (ticksSinceImageChanged > ticksPerImage)
		{
			ticksSinceImageChanged = 0;
			changeToNextImage();
		}
		
		for (var c: screensaverCubes)
		{
			c.move();
		}
	}
	
	private void changeToNextImage()
	{
		if (fileNamesNotYetShown.isEmpty())
		{
			reloadImagesInLoadingFolder();
		}
		
		if (fileNamesNotYetShown.isEmpty() == false)
		{
			oldImage = currentImage;
			var filePath = 
				_fileHelper.folders[Mode.Loading.ordinal()]
				+ "/" + fileNamesNotYetShown.removeFirst();
			try
			{
				currentImage = _fileHelper.readImageFromFileAtPath(filePath);
			}
			catch (Exception e)
			{
				currentImage = null;
				e.printStackTrace();
			}
		}
	}

	private void reloadImagesInLoadingFolder()
	{
		var folder = _fileHelper.folders[Mode.Loading.ordinal()];
		if (folder.exists())
		{
			var rand = new Random();
			for (var f: folder.listFiles())
			{
				if (_fileHelper.isFileAnImage(f))
				{
					var name = f.getName();
					var fileCount = fileNamesNotYetShown.size();
					var fileIndex = rand.nextInt(fileCount + 1);
					if (fileIndex == fileCount)
					{
						fileNamesNotYetShown.add(name);
					}
					else
					{
						fileNamesNotYetShown.add(fileIndex, name);
					}
				}
			}
		}
	}

	private void restart(boolean shouldImageBeChangedFirst)
	{
		paintThread.interrupt();
		try
		{
			paintThread.join();
		} catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}
		if (shouldImageBeChangedFirst)
		{
			changeToNextImage();
		}
		paintThread = new Thread("paintThread")
		{
			@Override
			public void run()
			{
				while (isMainDisplay)
				{
					try
					{
						motion();
						sleep(millisecondsPerTick);
					}
					catch (InterruptedException e)
					{
						break;
					}
				}
			}
		};
		paintThread.start();
	}
}
