package control;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import common.*;
import display.*;

public class ControlPictures extends Control
{
	private PicturePanel picturePanel;
	private final File imageFolder;
	private final DisplayPictures display;

	public ControlPictures
	(
		File imageFolder, DisplayPictures display, boolean areMultipleImagesAllowed
	)
	{
		super();
		this.imageFolder = imageFolder;
		this.display = display;

		var northPanel = getNorthPanel();
		northPanel.add(createScaleComboBox());
		northPanel.add(createFlipButton());
		add(northPanel, BorderLayout.NORTH);

		this.picturePanel = new PicturePanel(display, areMultipleImagesAllowed);
		add(createPicturePanelScrollPane(this.picturePanel), BorderLayout.CENTER);
		
		load();

		this.picturePanel.forgetThumbnails();

		setVisible(true);
		
	}
	
	private JComboBox<ScaleMethod> createScaleComboBox()
	{
		var scaleMethods = ScaleMethod.values();
		var scaleComboBox = _controlBuilder.createComboBox
		(
			scaleMethods, _controlBuilder.colors.controlBackground
		);
		scaleComboBox.setMaximumSize(new Coords(100, 5000).toDimension());
		scaleComboBox.setSelectedItem(ScaleMethod.UpScale);
		scaleComboBox.addActionListener
		(
			e -> { display.setScaleMode(scaleComboBox.getSelectedItem()); }
		);
		
		return scaleComboBox;
	}
	
	public JButton createFlipButton()
	{
		var flipButton = _controlBuilder.createButton
		(
			_paintHelper.icons.Flip,
			_controlBuilder.colors.controlBackground,
			arg0 ->
			{
				display.toggleShouldImageBeRotatedAHalfTurn();
			}
		);
		return flipButton;
	}
	
	public JScrollPane createPicturePanelScrollPane(PicturePanel picturePanel)
	{
		var jsp = _controlBuilder.createScrollPane(picturePanel);
		jsp.setBackground(_controlBuilder.colors.controlBackground);
		jsp.setBorder(BorderFactory.createEmptyBorder());
		return jsp;
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
			var fileHelper = _fileHelper;
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
				new PicturePanelButtonCreator(picturePanel, imageFolder);
			picturePanelButtonCreator.run();
			
			repaint();
			revalidate();
			display.removeAllImages();
			picturePanel.rememberThumbnails(imageFolder);
		}
	}
}
