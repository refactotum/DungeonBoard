package display;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

import common.*;
import main.*;

public class AlphaImage
{
	private String name;
	private File file;

	private ControlBuilder _controlBuilder = ControlBuilder.Instance;
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
	
	public ImageWrapper getImage()
	{
		ImageWrapper returnValue = null;

		var timesToAttempt = 50;
		for (int i = 0; i < timesToAttempt; i++)
		{
			try
			{
				returnValue = new ImageWrapper(ImageIO.read(file));
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
				_controlBuilder.showError(Main.Instance.getControl(), "Cannot load Image \"" + name, e);
			}
		}
		_controlBuilder.showError
		(
			Main.Instance.getControl(),
			"Cannot Load Image\"" + name + "\" after " + timesToAttempt
			+ " attempts.\n" + "Allocate more memory or use smaller images."
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
			_controlBuilder.showError(Main.Instance.getControl(), "Cannot load Image RGB \"" + name, e);
		}
		
		return returnValue;
	}
}
