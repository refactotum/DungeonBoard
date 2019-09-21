package display;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import main.*;

public class AlphaImage
{
	private String name;
	private File file;

	private Settings _settings;
	private ErrorHelper _errorHelper = ErrorHelper.Instance;

	public AlphaImage(Settings settings, File folder, String n)
	{
		_settings = settings;
		name = n;
		file = new File(folder.getAbsolutePath() + File.separator + name);
	}
	
	public String getName()
	{
		return name;
	}
	
	public BufferedImage getImage()
	{
		BufferedImage returnValue = null;

		for (int i = 0; i < 50; i++)
		{
			try
			{
				returnValue = ImageIO.read(file);
				break;
			} 
			catch (OutOfMemoryError e)
			{
				try
				{
					Thread.sleep(10);
				} 
				catch (InterruptedException e1)
				{
					// Do nothing.
				}
			}
			catch (IllegalArgumentException | IOException e)
			{
				_errorHelper.showError("Cannot load Image \"" + name, e);
			}
		}
		_errorHelper.showError
		(
			"Cannot Load Image\"" + name + "\" after 50 attempts\n" + "Allocate more memory, use smaller images"
		);
		
		return null;
	}
	
	public Color getBGColor()
	{
		var returnValue = Color.BLACK;
		
		var f = _settings.fileHelper.fileToThumb(file);
		
		try
		{
			returnValue = new Color(ImageIO.read(f).getRGB(0, 0));
		}
		catch (IllegalArgumentException | IOException e)
		{
			_errorHelper.showError("Cannot load Image RGB \"" + name, e);
		}
		
		return returnValue;
	}
}
