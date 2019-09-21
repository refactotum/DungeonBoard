package control;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import main.*;
import paint.*;

public class ControlPaint extends Control
{
	private DrawPanel drawPanel;
	private JComboBox<String> fileBox;
	private JTextField zoomText;
	private JSlider zoomSlider;
	private double maxZoom;
	private JPanel folderControlPanel;

	public ControlPaint()
	{
		super();

		var controlBuilder = _controlBuilder;
		var northPanel = controlBuilder.createPanelWithBoxLayout(BoxLayout.Y_AXIS);

		folderControlPanel = getEmptyNorthPanel();
		folderControlPanel.setVisible(false);

		var innerNorthPanel = getNorthPanel();

		maxZoom = 10.0;

		drawPanel = new DrawPanel();

		setFocusable(true);

		fileBox = controlBuilder.createComboBox(new String[] {}, controlBuilder.colors.controlBackground);
		fileBox.addItem("");
		var fileNames = getFileNames();
		for (var fileName : fileNames)
		{
			fileBox.addItem(fileName);
		}
		fileBox.addActionListener
		(
			e ->
			{
				if (fileBox.getSelectedIndex() != 0)
				{
					var filePath =
						_fileHelper.folders[Mode.Paint.ordinal()].getAbsolutePath()
						+ File.separator + fileBox.getSelectedItem().toString();

					var file = new File(filePath);
					
					if (file.exists())
					{
						drawPanel.saveMask();
						var maskFile = _fileHelper.fileToMaskFile(file);
						var dataFilePath =
							_fileHelper.dataFolder + File.separator 
							+ "Paint" + File.separator + maskFile.getName() 
							+ ".data";
						var dataFile = new File(dataFilePath);
						if (dataFile.exists())
						{
							try
							{
								var br = new BufferedReader(new FileReader(dataFile));
								
								var data = br.readLine().split(" ");
								var zoom = Double.parseDouble(data[0]);
								var p = new Point(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
								zoomSlider.setMaximum(10_000);
								zoomSlider.setValue((int) (zoom * 100));
								zoomText.setText(String.format("%.2f", zoom));
								drawPanel.setWindowScaleAndPosition(zoom, p);
								br.close();
							}
							catch (IOException e2)
							{
								_errorHelper.showError("Cannot load Mask Data", e2);
							}
						}
						if (file.isDirectory())
						{
							folderControlPanel.setVisible(true);
							setFolder(file);
						}
						else
						{
							folderControlPanel.setVisible(false);
							setFile(file);
						}
					}
					else
					{
						_errorHelper.showError("Cannot load Image, file does not exist");
					}
				}
			}
		);
		innerNorthPanel.add(fileBox);

		var icons = _paintHelper.icons;

		var iconsForPenDirectionLock = icons.PenDirectionLocks;
		var penDirectionLockButton = controlBuilder.createButton
		(
			iconsForPenDirectionLock[0],
			e ->
			{
				drawPanel.togglePenDirectionLock();
				((JButton)(e.getSource())).setIcon(iconsForPenDirectionLock[drawPanel.getStyle()]);
			}
		);
		innerNorthPanel.add(penDirectionLockButton);

		var iconsForPenShapes = icons.PenShapes;
		var shape = controlBuilder.createButton
		(
			iconsForPenShapes[0],
			e ->
			{
				drawPanel.togglePenShape();
				((JButton)(e.getSource())).setIcon(iconsForPenShapes[drawPanel.getPenShape()]);
			}
		);
		innerNorthPanel.add(shape);
		
		var iconsForTouchpadDrawMode = icons.TouchpadDrawModes;
		var touchpadDrawModeButton = controlBuilder.createButton
		(
			iconsForTouchpadDrawMode[0],
			e ->
			{
				drawPanel.toggleTouchpadDrawMode();
				((JButton)(e.getSource())).setIcon(iconsForTouchpadDrawMode[drawPanel.getTouchpadDrawMode()]);
			}
		);
		innerNorthPanel.add(touchpadDrawModeButton);
		
		var colors = controlBuilder.colors;

		var showButton = controlBuilder.createButton
		(
			"Show", colors.active, e -> { drawPanel.showAll(); }
		);
		innerNorthPanel.add(showButton);
		
		var hideButton = controlBuilder.createButton
		(
			"Hide", colors.inactive, e -> { drawPanel.hideAll(); }
		);
		innerNorthPanel.add(hideButton);
		
		var sliderPenRadius = controlBuilder.createSlider
		(
			SwingConstants.HORIZONTAL, 10, 100, 25,
			null, // minSize 
			colors.controlBackground,
			e ->
			{
				var slider = ((JSlider)(e.getSource()));
				drawPanel.setPenRadius(slider.getValue());
			}
		);
		innerNorthPanel.add(sliderPenRadius);
		
		innerNorthPanel.add(drawPanel.getUpdateButton());
		
		var westPanel = controlBuilder.createPanelWithBoxLayout
		(
			BoxLayout.Y_AXIS, colors.controlBackground
		);
		
		westPanel.add(controlBuilder.createLabel("Zoom", SwingConstants.LEFT));
		
		zoomText = controlBuilder.createTextField("1.00", 1);
		zoomText.setMaximumSize(new Dimension(5000, 25));
		zoomText.addActionListener
		(
			e ->
			{
				var zoom = 1.0;
				try
				{
					zoom = Double.parseDouble(zoomText.getText());
					if (zoom < 0.01) 
					{
						zoom = 0.01;
					}
					else if (zoom > maxZoom)
					{
						zoom = maxZoom;
					}
					drawPanel.setZoom(zoom);
				}
				catch (NumberFormatException nfe)
				{
					zoom = zoomSlider.getValue() / 100.0;
				}
				zoomText.setText(String.format("%.2f", zoom));
				zoomSlider.setValue((int) (zoom * 100));
			}
		);
		westPanel.add(zoomText);
		
		zoomSlider = controlBuilder.createSlider
		(
			SwingConstants.VERTICAL, 1, (int)(maxZoom * 100), 100,
			null, // minSize
			null, // backgroundColor
			e ->
			{
				var slider = ((JSlider)(e.getSource()));
				var zoom = slider.getValue() / 100.0;
				if (zoom < 0.01)
				{
					zoom = 0.01;
				}
				else if (zoom > maxZoom)
				{
					zoom = maxZoom;
				}
				zoomText.setText(String.format("%.2f", zoom));
				drawPanel.setZoom(zoom);
			}
		);
		westPanel.add(zoomSlider);
		
		northPanel.add(folderControlPanel);
		northPanel.add(innerNorthPanel);
		
		add(westPanel, BorderLayout.WEST);
		add(northPanel, BorderLayout.NORTH);
		add(drawPanel, BorderLayout.CENTER);
		
		setVisible(true);
	}
	
	private void setFolder(File paintFolder)
	{
		var files = paintFolder.listFiles(File::isFile);
		var paintFolderSize = 0;
		
		for (var f : files)
		{
			var fileName = f.getName();
			var prefix = fileName.substring(0, fileName.lastIndexOf('.'));
			var suffix = fileName.substring(fileName.lastIndexOf('.') + 1);
			try
			{
				if (suffix.equalsIgnoreCase("PNG") && Integer.parseInt(prefix) == paintFolderSize + 1)
				{
					paintFolderSize++;
				}
			}
			catch (NumberFormatException e)
			{
				if (fileName.equalsIgnoreCase("Guide.png") == false)
				{
					System.out.println
					(
						"File not in correct format: " + fileName + 
							". Should be named with a number, ex: '" + paintFolderSize + ".png'"
					);
				}
			}
		}

		var paintImages = new boolean[paintFolderSize];
		
		var controlBuilder = _controlBuilder;
		var colors = controlBuilder.colors;

		folderControlPanel.removeAll();
		// Creates all buttons.
		for (var i = 1; i <= paintFolderSize; i++)
		{
			var buttonI = controlBuilder.createButton
			(
				i + "", colors.inactive,
				e ->
				{
					var button = ((JButton)(e.getSource()));
					var number = Integer.parseInt(button.getText());
					
					if (button.getBackground().equals(colors.active))
					{
						button.setBackground(colors.inactive);
						paintImages[number - 1] = false;
					}
					else
					{
						button.setBackground(colors.active);
						paintImages[number - 1] = true;
					}
					var fileLoadingThread = new Thread("fileLoadingThread")
					{
						public void run()
						{
							try
							{
								var paintImage = _paintHelper.paintImage;
								synchronized (paintImage)
								{
									
									var g2d = paintImage.createGraphics();
									g2d.setColor(Color.BLACK);
									g2d.fillRect(0, 0, paintImage.getWidth(), paintImage.getHeight());
									
									var paintFolderSize = _fileHelper.paintFolderSize;
									for (var i = paintFolderSize; i > 0; i--)
									{
										if (paintImages[i - 1])
										{
											var f = new File(paintFolder + "/" + i + ".png");
											g2d.drawImage(ImageIO.read(f), 0, 0, null);
										}
									}
									
									g2d.dispose();
									
									if (_paintHelper.paintImage != null)
									{
										_main.displayPaint.setMask(drawPanel.getMask());
										_main.displayPaint.setImageSizeScaled();
										setZoomMax();
									}
								}
							}
							catch (IOException | OutOfMemoryError error)
							{
								drawPanel.resetImage();
								_main.displayPaint.resetImage();
								_paintHelper.paintImage = null;
								_errorHelper.showError("Cannot load Image, file is probably too large", error);
							}
							_main.displayPaint.repaint();
							drawPanel.repaint();
							drawPanel.setIsImageLoading(false);
						}
					};
					fileLoadingThread.start();
				}
			);
			
			folderControlPanel.add(buttonI);
		}
		folderControlPanel.revalidate();
		
		var guide = new File(paintFolder.getAbsolutePath() + "/Guide.png");

		if (paintFolder != null && paintFolder.exists()) // todo - Can this ever be null?
		{
			drawPanel.setIsImageLoading(true);
			var folderLoadingThread = new Thread("folderLoadingThread")
			{
				public void run()
				{
					try
					{
						_paintHelper.paintImage = null;
						_paintHelper.paintControlImage = null;
						Dimension imageSizeScaled;
						{
							var guideImg = ImageIO.read(guide);
							imageSizeScaled = new Dimension(guideImg.getWidth(), guideImg.getHeight());
							var paintGuideScale = _paintHelper.paintGuideScale;
							var paintControlImage = new BufferedImage
							(
									imageSizeScaled.width / paintGuideScale,
									imageSizeScaled.height / paintGuideScale,
									BufferedImage.TYPE_INT_RGB
							);
							_paintHelper.paintControlImage = paintControlImage;
							paintControlImage.getGraphics().drawImage
							(
								guideImg.getScaledInstance
								(
									imageSizeScaled.width / paintGuideScale,
									imageSizeScaled.height / paintGuideScale,
									BufferedImage.SCALE_SMOOTH
								), 
								0, 0, null
							);
						}
						_paintHelper.paintImage = new BufferedImage
						(
								imageSizeScaled.width,
								imageSizeScaled.height,
								BufferedImage.TYPE_INT_ARGB
							);
						drawPanel.setImage();
						_main.displayPaint.setMask(drawPanel.getMask());
						_main.displayPaint.setImageSizeScaled();
						setZoomMax();
					}
					catch (IOException | OutOfMemoryError error)
					{
						drawPanel.resetImage();
						_main.displayPaint.resetImage();
						_paintHelper.paintImage = null;
						_errorHelper.showError("Cannot load Image, file is probably too large", error);
					}
					_main.displayPaint.repaint();
					drawPanel.repaint();
					drawPanel.setIsImageLoading(false);
				}
			};
			folderLoadingThread.start();
		}
		
		_fileHelper.paintFolder = paintFolder;
		_fileHelper.paintFolderSize = paintFolderSize;
		_paintHelper.paintImageS = paintImages;
	}

	private void setFile(File file)
	{
		drawPanel.setIsImageLoading(true);
		_fileHelper.paintFolder = file;
		var fileLoadingThread = new Thread("fileLoadingThread")
		{
			public void run()
			{
				try
				{
					_paintHelper.paintImage = null;
					_paintHelper.paintImage = ImageIO.read(file);
					_paintHelper.paintControlImage = _paintHelper.paintImage;
					if (_paintHelper.paintImage != null)
					{
						drawPanel.setImage();
						_main.displayPaint.setMask(drawPanel.getMask());
						_main.displayPaint.setImageSizeScaled();
						setZoomMax();
					}
				}
				catch (IOException | OutOfMemoryError error)
				{
					drawPanel.resetImage();
					_main.displayPaint.resetImage();
					_paintHelper.paintImage = null;
					_errorHelper.showError("Cannot load Image, file is probably too large", error);
				}
				_main.displayPaint.repaint();
				drawPanel.repaint();
				drawPanel.setIsImageLoading(false);
			}
		};
		fileLoadingThread.start();
	}
	
	private void setZoomMax()
	{
		// Image cannot be smaller than the screen.
		var paintImage = _paintHelper.paintImage;
		var displaySize = _paintHelper.displaySize;
		var w = paintImage.getWidth() / displaySize.getWidth();
		var h = paintImage.getHeight() / displaySize.getHeight();
		maxZoom = h > w ? h : w;
		zoomSlider.setMaximum((int) (maxZoom * 100));
	}
	
	@Override
	protected void load()
	{
		// Do nothing?
	}
	
	private java.util.List<String> getFileNames()
	{
		var returnValues = new ArrayList<String>();

		var fileHelper = _fileHelper;

		var folder = fileHelper.folders[Mode.Paint.ordinal()];

		if (folder.exists())
		{
			for (var f : fileHelper.listFilesInOrder(folder))
			{
				var name = f.getName();
				if (f.isDirectory())
				{
					returnValues.add(name);
				}
				else if (fileHelper.isFileAnImage(f))
				{
					returnValues.add(name);
				}
			}
		}

		return returnValues;
	}

	public void saveMask()
	{
		drawPanel.saveMask();
	}
}
