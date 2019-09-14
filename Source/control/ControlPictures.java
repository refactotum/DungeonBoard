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

	public ControlPictures(Settings settings, File imageFolder, DisplayPictures display, boolean areMultipleImagesAllowed)
	{
		super(settings);
		this.imageFolder = imageFolder;
		this.display = display;
		
		var northPanel = getNorthPanel();

		var controlBuilder = _settings.controlBuilder;
		var colors = _settings.colors;
		var colorControlBackground = colors.CONTROL_BACKGROUND;

		var scaleMethods = ScaleMethod.values();
		var scaleComboBox = controlBuilder.createComboBox
		(
			scaleMethods, colorControlBackground
		);
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
		
		var flipButton = controlBuilder.createButton
		(
			_settings.icons.Flip,
			colorControlBackground,
			arg0 ->
			{
				display.toggleShouldImageBeRotatedAHalfTurn();
			}
		);
		northPanel.add(flipButton);
		
		picturePanel = new PicturePanel(_settings)
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
		
		var jsp = controlBuilder.createScrollPane(picturePanel);
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
			var fileHelper = _settings.fileHelper;
			var files =
				fileHelper.folderToDataFolder(imageFolder).listFiles();
			for (var file : files)
			{
				var fileFromThumbnail = fileHelper.thumbToFile(file);
				if (fileFromThumbnail.exists() == false)
				{
					file.delete();
				}
			}
			
			picturePanel.clearButtons();
			
			var picturePanelButtonCreator =
				new PicturePanelButtonCreator(_settings, picturePanel, imageFolder);
			picturePanelButtonCreator.run();
			
			repaint();
			revalidate();
			display.removeAllImages();
			picturePanel.rememberThumbnails(imageFolder);
		}
	}
}
