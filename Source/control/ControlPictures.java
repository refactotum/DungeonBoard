package control;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import display.*;
import main.*;

public class ControlPictures extends Control
{
	private PicturePanel picturePanel;
	private final File imageFolder;
	private final DisplayPictures display;
	
	private Settings _settings = Settings.Instance;
	
	public ControlPictures(File imageFolder, DisplayPictures display, boolean areMultipleImagesAllowed)
	{
		this.imageFolder = imageFolder;
		this.display = display;
		
		var northPanel = getNorthPanel();
		
		var colors = _settings.colors;
		var colorControlBackground = colors.CONTROL_BACKGROUND;
		
		var scaleComboBox = new JComboBox<>(ScaleMethod.values());
		scaleComboBox.setBackground(colorControlBackground);
		scaleComboBox.setMaximumSize(new Dimension(100, 5000));
		scaleComboBox.setSelectedItem(ScaleMethod.UP_SCALE);
		scaleComboBox.addActionListener
		(
			e ->
			{
				display.setScaleMode(scaleComboBox.getSelectedItem());
			}
		);
		northPanel.add(scaleComboBox);
		
		var flipButton = _settings.createButton(_settings.icons.Flip);
		flipButton.setBackground(colorControlBackground);
		flipButton.addActionListener
		(
			arg0 ->
			{
				display.toggleShouldImageBeRotatedAHalfTurn();
			}
		);
		northPanel.add(flipButton);
		
		picturePanel = new PicturePanel()
		{
			@Override
			protected void select(String name)
			{
				if (areMultipleImagesAllowed == false)
				{
					display.removeAllImages();
					for (var c: getComponents())
					{
						c.setBackground(colors.DISABLE_COLOR);
					}
				}
				display.addImage(name);
			}

			@Override
			protected void deselect(String name)
			{
				display.removeImage(name);
			}
		};
		
		add(northPanel, BorderLayout.NORTH);
		
		var jsp = new JScrollPane(picturePanel);
		jsp.setBackground(colorControlBackground);
		jsp.setBorder(BorderFactory.createEmptyBorder());
		add(jsp, BorderLayout.CENTER);
		
		load();
		
		picturePanel.forgetThumbnails();
		
		setVisible(true);
	}
	
	@Override
	public void setIsMainControl(boolean value)
	{
		if (value)
		{
			picturePanel.rememberThumbnails(imageFolder);
		}
		else
		{
			picturePanel.forgetThumbnails();
		}
	}
	
	@Override
	protected void load()
	{
		if (imageFolder.exists())
		{
			var files = _settings.folderToDataFolder(imageFolder).listFiles();
			for (var file: files)
			{
				var fileFromThumbnail = _settings.thumbToFile(file);
				if (fileFromThumbnail.exists() == false)
				{
					file.delete();
				}
			}
			
			picturePanel.clearButtons();
			
			var picturePanelButtonCreator =
				new PicturePanelButtonCreator(picturePanel, imageFolder);
			picturePanelButtonCreator.run();
			
			repaint();
			revalidate();
			display.removeAllImages();
			picturePanel.rememberThumbnails(imageFolder);
		}
	}
}
