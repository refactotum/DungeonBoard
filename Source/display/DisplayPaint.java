package display;

import java.awt.*;
import java.awt.image.*;

import common.*;

public class DisplayPaint extends Display
{
	// Each pixel should be either Color.BLACK or transparent.
	private ImageWrapper mask;
	
	private Coords imageSizeScaled;
	
	// The negative of this will be the position to start drawing.
	private Coords windowPosition;
	
	 // Larger means zoomed out and a smaller image.
	private double scale;

	public DisplayPaint()
	{
		super();
		windowPosition = new Coords(0, 0);
		scale = 1;
		setVisible(true);
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		var g2d = ((Graphics2D) g);
		
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setColor(Color.BLACK);
		var displaySize = _paintHelper.displaySize;
		g2d.fillRect(0, 0, displaySize.x, displaySize.y);
		
		var paintImage = _paintHelper.paintImage;
		if (paintImage != null && mask != null && imageSizeScaled != null)
		{
			var images = new ImageWrapper[] { paintImage, mask };
			for (var image : images)
			{
				g2d.drawImage
				(
					image.systemImage, -windowPosition.x, -windowPosition.y,
					imageSizeScaled.x, imageSizeScaled.y, null
				);
			}
		}
		paintMouse(g2d);
		g2d.dispose();
	}
	
	public void setMask(ImageWrapper newMask)
	{
		mask = newMask;
		repaint();
	}
	
	public void setImageSizeScaled()
	{
		var paintImageSize = _paintHelper.paintImage.size();
		imageSizeScaled = new Coords
		(
			(int)(paintImageSize.x / scale),
			(int)(paintImageSize.y / scale)
		);
	}
	
	@Override
	public void setIsMainDisplay(boolean value)
	{
		if (value == true)
		{
			repaint();
		}
	}
	
	public void setWindowScaleAndPosition(double scale, Coords position)
	{
		this.scale = scale;
		if (_paintHelper.paintImage != null)
		{
			setImageSizeScaled();
			windowPosition = position;
			var displaySize = _paintHelper.displaySize;
			if (imageSizeScaled.x < displaySize.x)
			{
				windowPosition.x = (imageSizeScaled.x - displaySize.x) / 2;
			}
			if (imageSizeScaled.y < displaySize.y)
			{
				windowPosition.y = (imageSizeScaled.y - displaySize.y) / 2;
			}
		}
		repaint();
	}
	
	public void setWindowPosition(Coords value)
	{
		windowPosition = value;
		if (imageSizeScaled != null)
		{
			var size = Coords.fromDimension(getSize());
			if (imageSizeScaled.x < size.x)
			{
				windowPosition.x = (imageSizeScaled.x - size.x) / 2;
			}
			if (imageSizeScaled.y < size.y)
			{
				windowPosition.y = (imageSizeScaled.y - size.y) / 2;
			}
		}
		repaint();
	}

	public void resetImage()
	{
		mask = _paintHelper.blankCursor;
		repaint();
	}
}
