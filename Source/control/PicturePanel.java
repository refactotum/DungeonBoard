package control;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

import common.*;
import display.*;
import main.*;

public class PicturePanel extends JPanel
{
	private final int GridWidth = 4;
	private final Coords ImageIconSize = new Coords(100, 60);

	private ControlBuilder _controlBuilder = ControlBuilder.Instance;
	private FileHelper _fileHelper = FileHelper.Instance;

	private DisplayPictures _display;
	private boolean _areMultipleImagesAllowed;

	public PicturePanel(DisplayPictures display, boolean areMultipleImagesAllowed)
	{
		this._display = display;
		this._areMultipleImagesAllowed = areMultipleImagesAllowed;

		setLayout(new GridLayout(0, GridWidth));
		setBorder(BorderFactory.createEmptyBorder());
	}

	public JButton createPicturePanelButton(File file)
	{
		createThumbnail(file);

		var colors = _controlBuilder.colors;

		var buttonFile = _controlBuilder.createButton
		(
			file.getName(),
			colors.disableColor,
			e ->
			{
				var button = ((JButton)(e.getSource()));
				var name = button.getText();
				if (button.getBackground() == colors.disableColor)
				{
					select(name);
					button.setBackground(colors.enableColor);
				}
				else
				{
					deselect(name);
					button.setBackground(colors.disableColor);
				}
			}
		);
		buttonFile.setMargin(new Insets(0, 0, 0, 0));
		buttonFile.setFocusPainted(false);
		buttonFile.setVerticalTextPosition(SwingConstants.TOP);
		buttonFile.setHorizontalTextPosition(SwingConstants.CENTER);
		
		return buttonFile;
	}
	
	private void createThumbnail(File file)
	{
		var tFile = _fileHelper.fileToThumb(file);
		if (tFile.exists() == false || file.lastModified() > tFile.lastModified())
		{
			try
			{
				var imageIconSize = ImageIconSize;
				var bufferedImage = new BufferedImage
				(
					imageIconSize.x, imageIconSize.y, BufferedImage.TYPE_INT_RGB
				);
				bufferedImage.getGraphics().drawImage
				(
					ImageIO.read(file).getScaledInstance
					(
						imageIconSize.x, imageIconSize.y, BufferedImage.SCALE_SMOOTH
					),
					0, 0, null
				);
				ImageIO.write(bufferedImage, "GIF", tFile);
			}
			catch (OutOfMemoryError | IOException e)
			{
				_controlBuilder.showError(this, "Cannot create Thumbnail, file is probably too large", e);
				e.printStackTrace();
			}
		}
	}

	public void clearButtons()
	{
		for (var c : getComponents())
		{
			if (c.getClass().equals(JButton.class))
			{
				remove(c);
			}
		}
	}

	protected void select(String name)
	{
		if (this._areMultipleImagesAllowed == false)
		{
			this._display.removeAllImages();
			for (var c: getComponents())
			{
				c.setBackground(_controlBuilder.colors.disableColor);
			}
		}
		this._display.addImage(name);
	}

	protected void deselect(String name)
	{
		this._display.removeImage(name);
	}

	public void rememberThumbnails(File folder)
	{
		for (var c : getComponents())
		{
			if (c.getClass().equals(JButton.class))
			{
				var b = (JButton) c;
				var filePath = folder + File.separator + b.getText();
				var f = _fileHelper.getFileAtPath(filePath);
				f = _fileHelper.fileToThumb(f);
				try
				{
					b.setIcon(new ImageIcon(ImageIO.read(f)));
				}
				catch (OutOfMemoryError | IOException e)
				{
					_controlBuilder.showError(this, "Cannot load Thumbnail, file is probably too large", e);
					e.printStackTrace();
				}
			}
		}
	}

	public void forgetThumbnails()
	{
		for (Component c: getComponents())
		{
			if (c.getClass().equals(JButton.class))
			{
				var b = (JButton) c;
				b.setIcon(null);
			}
		}
	}
}
