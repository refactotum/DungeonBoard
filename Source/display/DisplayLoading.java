package display;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import common.*;
import main.*;

public class DisplayLoading extends Display
{
	private static final int ticksPerSecond = 20;
	private static final int millisecondsPerTick = 1000 / ticksPerSecond;
	private static final int ticksPerFade = 20;

	private int ticksPerImage = 400;
	private LinkedList<ScreensaverCube> screensaverCubes;
	private LinkedList<String> fileNamesNotYetShown;
	private ImageWrapper oldImage;
	private ImageWrapper currentImage;
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
			paintCurrentImage(g2d);
		}
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

		for (var c: screensaverCubes)
		{
			c.paint(g2d);
		}

		paintMouse(g2d);

		g2d.dispose();
	}
	
	private void paintCurrentImage(Graphics2D g2d)
	{
		if (shouldImagesBeUpscaled)
		{
			paintCurrentImageUpscaled(g2d);
		}
		else
		{
			paintCurrentImageNotUpscaled(g2d);
		}
	}
	
	private void paintCurrentImageUpscaled(Graphics2D g2d)
	{
		var displaySize = _paintHelper.displaySize;

		if (ticksSinceImageChanged <= ticksPerFade)
		{
			g2d.drawImage(oldImage.systemImage, 0, 0, displaySize.x, displaySize.y, null);
		}
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeOpacity));
		g2d.drawImage(currentImage.systemImage, 0, 0, displaySize.x, displaySize.y, null);
	}

	private void paintCurrentImageNotUpscaled(Graphics2D g2d)
	{
		var displaySize = _paintHelper.displaySize;

		g2d.setColor(new Color(currentImage.systemImage.getRGB(0, 0)));
		g2d.fillRect(0, 0, displaySize.x, displaySize.y);

		if (ticksSinceImageChanged <= ticksPerFade && oldImage != null)
		{
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - fadeOpacity));
			var oldImageSize = oldImage.size();
			g2d.drawImage
			(
				oldImage.systemImage,
				(displaySize.x - oldImageSize.x) / 2,
				(displaySize.y - oldImageSize.y) / 2,
				null
			);
		}
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeOpacity));
		var currentImageSize = currentImage.size();
		g2d.drawImage
		(
			currentImage.systemImage, 
			(displaySize.x - currentImageSize.x) / 2,
			(displaySize.y - currentImageSize.y) / 2,
			null
		);
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
