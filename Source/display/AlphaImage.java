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

	private ErrorHelper _errorHelper = ErrorHelper.Instance;
	private FileHelper _fileHelper = FileHelper.Instance;

	public AlphaImage(File folder, String n)
	{
		name = n;
		var filePath = folder.getAbsolutePath() + File.separator + name;
		file = _fileHelper.getFileAtPath(filePath);
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
		
		var f = _fileHelper.fileToThumb(file);
		
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
