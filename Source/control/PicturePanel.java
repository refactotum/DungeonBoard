package control;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import main.*;

public abstract class PicturePanel extends JPanel
{
	private final int GRID_WIDTH = 4;
	private final Dimension IMAGE_ICON_SIZE = new Dimension(100, 60);

	private ControlBuilder _controlBuilder = ControlBuilder.Instance;
	private ErrorHelper _errorHelper = ErrorHelper.Instance;
	private FileHelper _fileHelper = FileHelper.Instance;

	public PicturePanel()
	{
		setLayout(new GridLayout(0, GRID_WIDTH));
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
				var imageIconSize = IMAGE_ICON_SIZE;
				var bufferedImage = new BufferedImage
				(
					imageIconSize.width, imageIconSize.height, BufferedImage.TYPE_INT_RGB
				);
				bufferedImage.getGraphics().drawImage
				(
					ImageIO.read(file).getScaledInstance
					(
						imageIconSize.width, imageIconSize.height, BufferedImage.SCALE_SMOOTH
					),
					0, 0, null
				);
				ImageIO.write(bufferedImage, "GIF", tFile);
			}
			catch (OutOfMemoryError | IOException e)
			{
				_errorHelper.showError("Cannot create Thumbnail, file is probably too large", e);
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
	
	protected abstract void select(String name);
	
	protected abstract void deselect(String name);

	public void rememberThumbnails(File folder)
	{
		for (var c : getComponents())
		{
			if (c.getClass().equals(JButton.class))
			{
				var b = (JButton) c;
				var f = new File(folder + File.separator + b.getText());
				f = _fileHelper.fileToThumb(f);
				try
				{
					b.setIcon(new ImageIcon(ImageIO.read(f)));
				}
				catch (OutOfMemoryError | IOException e)
				{
					_errorHelper.showError("Cannot load Thumbnail, file is probably too large", e);
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
