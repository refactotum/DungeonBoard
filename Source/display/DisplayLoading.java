package display;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;

import main.*;

public class DisplayLoading extends Display
{
	private static final int TICKS_PER_SECOND = 20;
	private static final int MILLISECONDS_PER_TICK = 1000 / TICKS_PER_SECOND;
	private static final int TICKS_PER_FADE = 20;

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

	public DisplayLoading(Settings settings)
	{
		super(settings);
		screensaverCubes = new LinkedList<>();
		paintThread = new Thread();
		fileNamesNotYetShown = new LinkedList<>();
		shouldImagesBeUpscaled = false;
		ticksSinceImageChanged = TICKS_PER_FADE;
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
			var displaySize = _settings.DISPLAY_SIZE;
			
			if (shouldImagesBeUpscaled)
			{
				if (ticksSinceImageChanged <= TICKS_PER_FADE)
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

				if (ticksSinceImageChanged <= TICKS_PER_FADE && oldImage != null)
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
		ticksPerImage = value * TICKS_PER_SECOND;
	}

	public void setShouldImagesBeUpscaled(boolean value)
	{
		shouldImagesBeUpscaled = value;
		repaint();
	}
	
	public void addScreensaverCube()
	{
		screensaverCubes.add(new ScreensaverCube(_settings));
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
		if (ticksSinceImageChanged <= TICKS_PER_FADE)
		{
			fadeOpacity = (float)ticksSinceImageChanged / TICKS_PER_FADE;
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
				_settings.fileHelper.FOLDERS[Mode.LOADING.ordinal()]
				+ "/" + fileNamesNotYetShown.removeFirst();
			try
			{
				currentImage = ImageIO.read(new File(filePath));
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
		var folder = _settings.fileHelper.FOLDERS[Mode.LOADING.ordinal()];
		if (folder.exists())
		{
			var rand = new Random();
			for (var f: folder.listFiles())
			{
				var name = f.getName();
				var suffix = name.substring(name.lastIndexOf('.') + 1);
				if (suffix.equalsIgnoreCase("PNG") || suffix.equalsIgnoreCase("JPG") || suffix.equalsIgnoreCase("JPEG"))
				{
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
						sleep(MILLISECONDS_PER_TICK);
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
