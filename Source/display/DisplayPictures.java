package display;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class DisplayPictures extends Display
{
	private LinkedList<AlphaImage> imagesToBePainted;
	private BufferedImage image;
	private ScaleMethod scaleMethod;
	private boolean shouldImageBeRotatedAHalfTurn;
	private Thread compileThread;
	private final File imageFolder;

	public DisplayPictures(File imageFolder)
	{
		super();
		this.imageFolder = imageFolder;

		var displaySize = _paintHelper.displaySize;
		image = new BufferedImage(displaySize.width, displaySize.height, BufferedImage.TYPE_INT_ARGB);
		imagesToBePainted = new LinkedList<>();
		scaleMethod = ScaleMethod.UpScale;
		shouldImageBeRotatedAHalfTurn = false;
		
		setVisible(true);
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		var g2d = (Graphics2D) g;
		drawImage(g2d, image);
		paintMouse(g2d);
		g2d.dispose();
	}
	
	public void paintImage(Graphics2D g2d, BufferedImage img)
	{
		var displaySize = _paintHelper.displaySize;
		if (scaleMethod == ScaleMethod.Fill)
		{
			g2d.drawImage(img, 0, 0, displaySize.width, displaySize.height, null);
		}
		else if (scaleMethod == ScaleMethod.RealSize)
		{
			g2d.drawImage
			(
				img,
				(displaySize.width - img.getWidth()) / 2,
				(displaySize.height - img.getHeight()) / 2,
				img.getWidth(),
				img.getHeight(), null
			);
		}
		else if (scaleMethod == ScaleMethod.UpScale)
		{
			this.paintImageUpscaled(g2d, img, displaySize);
		}
	}
	
	private void paintImageUpscaled(Graphics2D g2d, BufferedImage img, Dimension displaySize)
	{
		var screenRatio = displaySize.getWidth() / displaySize.getHeight();
		var imageRatio = (double)img.getWidth() / img.getHeight();
		Dimension imageScale;
		if (imageRatio > screenRatio)
		{
			// width > height
			imageScale = new Dimension(displaySize.width, (int) (displaySize.width / imageRatio));
		}
		else
		{
			// width < height
			imageScale = new Dimension((int) (displaySize.height * imageRatio), displaySize.height);
		}
		g2d.drawImage
		(
			img,
			(displaySize.width - imageScale.width) / 2,
			(displaySize.height - imageScale.height) / 2,
			imageScale.width,
			imageScale.height, null
		);
	}
	
	private void fillBackground(Graphics2D g2d, Color c)
	{
		g2d.setColor(c);
		g2d.fillRect(0, 0, _paintHelper.displaySize.width, _paintHelper.displaySize.height);
	}
	
	private void drawImage(Graphics2D g2d, BufferedImage img)
	{
		if (shouldImageBeRotatedAHalfTurn)
		{
			var oldAT = g2d.getTransform();
			var at = new AffineTransform();
			at.rotate(Math.PI, getWidth() / 2, getHeight() / 2);
			g2d.setTransform(at);
			g2d.drawImage(img, 0, 0, null);
			g2d.setTransform(oldAT);
		}
		else
		{
			g2d.drawImage(img, 0, 0, null);
		}
	}
	
	public void addImage(String name)
	{
		AlphaImage ai = new AlphaImage(imageFolder, name);
		stopCompile();
		imagesToBePainted.add(ai);
		compileImage();
		repaint();
	}
	
	private void stopCompile()
	{
		if (compileThread != null && compileThread.isAlive())
		{
			compileThread.interrupt();
		}
	}
	
	private void compileImage()
	{
		compileThread = new Thread("compileImage")
		{
			@Override
			public void run()
			{
				var displaySize = _paintHelper.displaySize;
				var img = new BufferedImage
				(
					displaySize.width,
					displaySize.height,
					BufferedImage.TYPE_INT_ARGB
				);
				var g2d = img.createGraphics();
				if (imagesToBePainted.size() == 0)
				{
					fillBackground(g2d, Color.BLACK);
				}
				else
				{
					fillBackground(g2d, imagesToBePainted.getFirst().getBGColor());
					try
					{
						for (var image: imagesToBePainted)
						{
							paintImage(g2d, image.getImage());
						}
					}
					catch (NullPointerException | ConcurrentModificationException e)
					{
						return;
					}
				}
				g2d.dispose();
				if (isInterrupted())
				{
					return;
				}
				image = img;
				repaint();
			}
		};
		compileThread.start();
	}

	public void removeImage(String name)
	{
		stopCompile();
		for (int i = 0; i < imagesToBePainted.size();) // Weird.
		{
			if (imagesToBePainted.get(i).getName().equals(name))
			{
				imagesToBePainted.remove(i);
			}
			else
			{
				i++;
			}
		}
		compileImage();
		repaint();
	}

	public void setScaleMode(Object selectedItem)
	{
		stopCompile();
		scaleMethod = (ScaleMethod) selectedItem;
		compileImage();
		repaint();
	}
	
	public void removeAllImages()
	{
		stopCompile();
		imagesToBePainted.clear();
		compileImage();
		repaint();
	}

	public void toggleShouldImageBeRotatedAHalfTurn()
	{
		shouldImageBeRotatedAHalfTurn = (shouldImageBeRotatedAHalfTurn == false);
		repaint();
	}
	
	@Override
	public synchronized void setIsMainDisplay(boolean value)
	{
		if (value == true)
		{
			stopCompile();
			compileImage();
		}
		else
		{
			image = null;
		}
	}
}
