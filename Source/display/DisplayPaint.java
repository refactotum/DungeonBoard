package display;
import java.awt.*;
import java.awt.image.*;
import main.*;

public class DisplayPaint extends Display
{
	// Each pixel should be either Color.BLACK or transparent.
	private BufferedImage mask;
	
	private Dimension imageSizeScaled;
	
	// The negative of this will be the position to start drawing.
	private Point windowPosition;
	
	 // Larger means zoomed out and a smaller image.
	private double scale;

	public DisplayPaint()
	{
		windowPosition = new Point(0, 0);
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
		var displaySize = _settings.DISPLAY_SIZE;
		g2d.fillRect(0, 0, displaySize.width, displaySize.height);
		
		var paintImage = _settings.PAINT_IMAGE;
		if (paintImage != null && mask != null && imageSizeScaled != null)
		{
			var images = new BufferedImage[] { paintImage, mask };
			for (var image : images)
			{
				g2d.drawImage
				(
					image, -windowPosition.x, -windowPosition.y, imageSizeScaled.width, imageSizeScaled.height, null
				);
			}
		}
		paintMouse(g2d);
		g2d.dispose();
	}
	
	public void setMask(BufferedImage newMask)
	{
		mask = newMask;
		repaint();
	}
	
	public void setImageSizeScaled()
	{
		var paintImage = _settings.PAINT_IMAGE;
		imageSizeScaled = new Dimension
		(
			(int)(paintImage.getWidth() / scale),
			(int)(paintImage.getHeight() / scale)
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
	
	public void setWindowScaleAndPosition(double scale, Point position)
	{
		this.scale = scale;
		if (_settings.PAINT_IMAGE != null)
		{
			setImageSizeScaled();
			windowPosition = position;
			var displaySize = _settings.DISPLAY_SIZE;
			if (imageSizeScaled.width < displaySize.width)
			{
				windowPosition.x = (imageSizeScaled.width - displaySize.width) / 2;
			}
			if (imageSizeScaled.height < _settings.DISPLAY_SIZE.height)
			{
				windowPosition.y = (imageSizeScaled.height - displaySize.height) / 2;
			}
		}
		repaint();
	}
	
	public void setWindowPosition(Point value)
	{
		windowPosition = value;
		if (imageSizeScaled != null)
		{
			var size = getSize();
			if (imageSizeScaled.width < size.width)
			{
				windowPosition.x = (imageSizeScaled.width - size.width) / 2;
			}
			if (imageSizeScaled.height < size.height)
			{
				windowPosition.y = (imageSizeScaled.height - size.height) / 2;
			}
		}
		repaint();
	}

	public void resetImage()
	{
		mask = _settings.BLANK_CURSOR;
		repaint();
	}
}
