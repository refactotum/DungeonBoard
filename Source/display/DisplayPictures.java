package display;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import common.*;

public class DisplayPictures extends Display
{
	private LinkedList<AlphaImage> imagesToBePainted;
	private ImageWrapper image;
	private ScaleMethod scaleMethod;
	private boolean shouldImageBeRotatedAHalfTurn;
	private Thread compileThread;
	private final File imageFolder;

	public DisplayPictures(File imageFolder)
	{
		super();
		this.imageFolder = imageFolder;

		var displaySize = _paintHelper.displaySize;
		image = new ImageWrapper
		(
			new BufferedImage(displaySize.x, displaySize.y, BufferedImage.TYPE_INT_ARGB)
		);
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
	
	public void paintImage(Graphics2D g2d, ImageWrapper img)
	{
		var displaySize = _paintHelper.displaySize;
		if (scaleMethod == ScaleMethod.Fill)
		{
			g2d.drawImage(img.systemImage, 0, 0, displaySize.x, displaySize.y, null);
		}
		else if (scaleMethod == ScaleMethod.RealSize)
		{
			var imageSize = img.size();
			g2d.drawImage
			(
				img.systemImage,
				(displaySize.x - imageSize.x) / 2,
				(displaySize.y - imageSize.y) / 2,
				imageSize.x,
				imageSize.y, null
			);
		}
		else if (scaleMethod == ScaleMethod.UpScale)
		{
			this.paintImageUpscaled(g2d, img, displaySize);
		}
	}
	
	private void paintImageUpscaled(Graphics2D g2d, ImageWrapper img, Coords displaySize)
	{
		var screenRatio = displaySize.x / displaySize.y;
		var imageSize = img.size();
		var imageRatio = (double)imageSize.x / imageSize.y;
		Coords imageScale;
		if (imageRatio > screenRatio)
		{
			// width > height
			imageScale = new Coords(displaySize.x, (int) (displaySize.x / imageRatio));
		}
		else
		{
			// width < height
			imageScale = new Coords((int) (displaySize.y * imageRatio), displaySize.y);
		}
		g2d.drawImage
		(
			img.systemImage,
			(displaySize.x - imageScale.x) / 2,
			(displaySize.y - imageScale.y) / 2,
			imageScale.x,
			imageScale.y, null
		);
	}
	
	private void fillBackground(Graphics2D g2d, Color c)
	{
		g2d.setColor(c);
		g2d.fillRect(0, 0, _paintHelper.displaySize.x, _paintHelper.displaySize.y);
	}
	
	private void drawImage(Graphics2D g2d, ImageWrapper img)
	{
		if (shouldImageBeRotatedAHalfTurn)
		{
			var oldAT = g2d.getTransform();
			var at = new AffineTransform();
			at.rotate(Math.PI, getWidth() / 2, getHeight() / 2);
			g2d.setTransform(at);
			g2d.drawImage(img.systemImage, 0, 0, null);
			g2d.setTransform(oldAT);
		}
		else
		{
			g2d.drawImage(img.systemImage, 0, 0, null);
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
				var img = new ImageWrapper
				(
					new BufferedImage
					(
						displaySize.x, displaySize.y,
						BufferedImage.TYPE_INT_ARGB
					)
				);
				var g2d = img.systemImage.createGraphics();
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
