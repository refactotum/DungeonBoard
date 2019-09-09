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
	private static final int GRID_WIDTH = 4;
	private static final Dimension IMAGE_ICON_SIZE = new Dimension(100, 60);
	
	private Settings _settings = Settings.Instance;

	public PicturePanel()
	{
		setLayout(new GridLayout(0, GRID_WIDTH));
		setBorder(BorderFactory.createEmptyBorder());
	}
	
	public JButton createPicturePanelButton(File file)
	{
		createThumbnail(file);
		
		var colors = _settings.colors;
		
		var button = new JButton(file.getName());
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusPainted(false);
		button.setVerticalTextPosition(SwingConstants.TOP);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setBackground(colors.DISABLE_COLOR);
		button.addActionListener
		(
			arg0 ->
			{
				var name = button.getText();
				if (button.getBackground() == colors.DISABLE_COLOR)
				{
					select(name);
					button.setBackground(colors.ENABLE_COLOR);
				}
				else
				{
					deselect(name);
					button.setBackground(colors.DISABLE_COLOR);
				}
			}
		);
		return button;
	}
	
	private void createThumbnail(File file)
	{
		var tFile = _settings.fileToThumb(file);
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
				_settings.showError("Cannot create Thumbnail, file is probably too large", e);
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
				f = _settings.fileToThumb(f);
				try
				{
					b.setIcon(new ImageIcon(ImageIO.read(f)));
				}
				catch (OutOfMemoryError | IOException e)
				{
					_settings.showError("Cannot load Thumbnail, file is probably too large", e);
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
