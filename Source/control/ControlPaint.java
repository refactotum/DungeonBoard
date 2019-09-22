package control;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

import common.*;
import display.*;
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
	private DisplayPaint _displayPaint;

	public ControlPaint(DisplayPaint displayPaint)
	{
		super();
		this._displayPaint = displayPaint;

		var northPanel = _controlBuilder.createPanelWithBoxLayout(BoxLayout.Y_AXIS);

		folderControlPanel = getEmptyNorthPanel();
		folderControlPanel.setVisible(false);

		maxZoom = 10.0;
		setFocusable(true);

		var innerNorthPanel = getNorthPanel();
		innerNorthPanel.add(createFileBox());
		innerNorthPanel.add(createPenDirectionLockButton());
		innerNorthPanel.add(createPenShapeButton());
		innerNorthPanel.add(createTouchpadDrawModeButton());
		innerNorthPanel.add(createShowButton());
		innerNorthPanel.add(createHideButton());
		innerNorthPanel.add(createSliderPenRadius());
		drawPanel = new DrawPanel();
		innerNorthPanel.add(drawPanel.getUpdateButton());

		var westPanel = createWestPanel();

		northPanel.add(folderControlPanel);
		northPanel.add(innerNorthPanel);

		add(westPanel, BorderLayout.WEST);
		add(northPanel, BorderLayout.NORTH);
		add(drawPanel, BorderLayout.CENTER);

		setVisible(true);
	}

	private JComboBox<String> createFileBox()
	{
		fileBox = _controlBuilder.createComboBox
		(
			new String[] {}, _controlBuilder.colors.controlBackground
		);
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

					var file = _fileHelper.getFileAtPath(filePath);
					
					if (file.exists())
					{
						drawPanel.saveMask();
						var maskFile = _fileHelper.fileToMaskFile(file);
						var dataFilePath =
							_fileHelper.dataFolder + File.separator 
							+ "Paint" + File.separator + maskFile.getName() 
							+ ".data";
						var dataFile = _fileHelper.getFileAtPath(dataFilePath);
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
								_controlBuilder.showError(this, "Cannot load Mask Data", e2);
							}
						}
						if (file.isDirectory())
						{
							folderControlPanel.setVisible(true);
							setupPaintFolder(file);
						}
						else
						{
							folderControlPanel.setVisible(false);
							setFile(file);
						}
					}
					else
					{
						_controlBuilder.showError(this, "Cannot load Image, file does not exist");
					}
				}
			}
		);
		return fileBox;
	}
	
	private JButton createPenDirectionLockButton()
	{
		var icons = _paintHelper.icons;
		var iconsForPenDirectionLock = icons.PenDirectionLocks;
		var penDirectionLockButton = _controlBuilder.createButton
		(
			iconsForPenDirectionLock[0],
			e ->
			{
				drawPanel.togglePenDirectionLock();
				((JButton)(e.getSource())).setIcon(iconsForPenDirectionLock[drawPanel.getStyle()]);
			}
		);
		return penDirectionLockButton;
	}
	
	private JButton createPenShapeButton()
	{
		var icons = _paintHelper.icons;
		var iconsForPenShapes = icons.PenShapes;
		var penShapeButton = _controlBuilder.createButton
		(
			iconsForPenShapes[0],
			e ->
			{
				drawPanel.togglePenShape();
				((JButton)(e.getSource())).setIcon(iconsForPenShapes[drawPanel.getPenShape()]);
			}
		);
		return penShapeButton;
	}
	
	private JButton createTouchpadDrawModeButton()
	{
		var icons = _paintHelper.icons;
		var iconsForTouchpadDrawMode = icons.TouchpadDrawModes;
		var touchpadDrawModeButton = _controlBuilder.createButton
		(
			iconsForTouchpadDrawMode[0],
			e ->
			{
				drawPanel.toggleTouchpadDrawMode();
				((JButton)(e.getSource())).setIcon(iconsForTouchpadDrawMode[drawPanel.getTouchpadDrawMode()]);
			}
		);
		return touchpadDrawModeButton;
	}
	
	private JButton createShowButton()
	{
		var showButton = _controlBuilder.createButton
		(
			"Show", _controlBuilder.colors.active, e -> { drawPanel.showAll(); }
		);
		return showButton;
	}
	
	private JButton createHideButton()
	{
		var hideButton = _controlBuilder.createButton
		(
			"Hide", _controlBuilder.colors.inactive, e -> { drawPanel.hideAll(); }
		);
		return hideButton;
	}
	
	private JSlider createSliderPenRadius()
	{
		var sliderPenRadius = _controlBuilder.createSlider
		(
			SwingConstants.HORIZONTAL, 10, 100, 25,
			null, // minSize 
			_controlBuilder.colors.controlBackground,
			e ->
			{
				var slider = ((JSlider)(e.getSource()));
				drawPanel.setPenRadius(slider.getValue());
			}
		);
		return sliderPenRadius;
	}
	
	private JPanel createWestPanel()
	{
		var westPanel = _controlBuilder.createPanelWithBoxLayout
		(
			BoxLayout.Y_AXIS, _controlBuilder.colors.controlBackground
		);
		
		westPanel.add(_controlBuilder.createLabel("Zoom", SwingConstants.LEFT));
		westPanel.add(createZoomText());
		westPanel.add(createZoomSlider());
		
		return westPanel;
	}

	private JTextField createZoomText()
	{
		zoomText = _controlBuilder.createTextField("1.00", 1);
		zoomText.setMaximumSize(new Coords(5000, 25).toDimension());
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
		return zoomText;
	}

	private JSlider createZoomSlider()
	{
		zoomSlider = _controlBuilder.createSlider
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
		return zoomSlider;
	}

	private void setupPaintFolder(File paintFolder)
	{
		var paintFolderSize = findPaintFolderSize(paintFolder);
		var shouldImagesBePainted = createPaintFolderButtons(paintFolder, paintFolderSize);
		createAndStartFolderLoadingThread(paintFolder);
		
		_fileHelper.paintFolder = paintFolder;
		_fileHelper.paintFolderSize = paintFolderSize;
		_paintHelper.shouldImagesBePainted = shouldImagesBePainted;
	}
	
	private int findPaintFolderSize(File paintFolder)
	{
		var files = paintFolder.listFiles(File::isFile);
		var paintFolderSize = 0;
		
		for (var f : files)
		{
			var fileName = f.getName();
			var lastIndexOfDot = fileName.lastIndexOf('.');
			var prefix = fileName.substring(0, lastIndexOfDot);
			var suffix = fileName.substring(lastIndexOfDot + 1);
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
		
		return paintFolderSize;
	}
	
	private boolean[] createPaintFolderButtons(File paintFolder, int paintFolderSize)
	{
		var shouldImagesBePainted = new boolean[paintFolderSize];

		var controlBuilder = _controlBuilder;
		var colors = controlBuilder.colors;
		var controlPaint = this;

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
						shouldImagesBePainted[number - 1] = false;
					}
					else
					{
						button.setBackground(colors.active);
						shouldImagesBePainted[number - 1] = true;
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
									var g2d = paintImage.systemImage.createGraphics();
									g2d.setColor(Color.BLACK);
									var paintImageSize = paintImage.size();
									g2d.fillRect(0, 0, paintImageSize.x, paintImageSize.y);
									
									var paintFolderSize = _fileHelper.paintFolderSize;
									for (var i = paintFolderSize; i > 0; i--)
									{
										if (shouldImagesBePainted[i - 1])
										{
											var filePath = paintFolder + "/" + i + ".png";
											var f = _fileHelper.getFileAtPath(filePath);
											g2d.drawImage(ImageIO.read(f), 0, 0, null);
										}
									}
									
									g2d.dispose();
									
									if (_paintHelper.paintImage != null)
									{
										_displayPaint.setMask(drawPanel.getMask());
										_displayPaint.setImageSizeScaled();
										setZoomMax();
									}
								}
							}
							catch (IOException | OutOfMemoryError error)
							{
								drawPanel.resetImage();
								_displayPaint.resetImage();
								_paintHelper.paintImage = null;
								_controlBuilder.showError
								(
									controlPaint,
									"Cannot load Image, file is probably too large",
									error
								);
							}
							_displayPaint.repaint();
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
		
		return shouldImagesBePainted;
	}
	
	private void createAndStartFolderLoadingThread(File paintFolder)
	{
		var filePath = paintFolder.getAbsolutePath() + "/Guide.png";
		var guide = _fileHelper.getFileAtPath(filePath);

		if (paintFolder != null && paintFolder.exists()) // todo - Can this ever be null?
		{
			drawPanel.setIsImageLoading(true);
			var controlPaint = this;
			var folderLoadingThread = new Thread("folderLoadingThread")
			{
				public void run()
				{
					try
					{
						_paintHelper.paintImage = null;
						_paintHelper.paintControlImage = null;
						Coords imageSizeScaled;
						{
							var guideImg = ImageIO.read(guide);
							imageSizeScaled = new Coords(guideImg.getWidth(), guideImg.getHeight());
							var paintGuideScale = _paintHelper.paintGuideScale;
							var paintControlImage = new ImageWrapper
							(
								new BufferedImage
								(
									imageSizeScaled.x / paintGuideScale,
									imageSizeScaled.y / paintGuideScale,
									BufferedImage.TYPE_INT_RGB
								)
							);
							_paintHelper.paintControlImage = paintControlImage;
							paintControlImage.systemImage.getGraphics().drawImage
							(
								guideImg.getScaledInstance
								(
									imageSizeScaled.x / paintGuideScale,
									imageSizeScaled.y / paintGuideScale,
									BufferedImage.SCALE_SMOOTH
								), 
								0, 0, null
							);
						}
						_paintHelper.paintImage = new ImageWrapper
						(
							new BufferedImage
							(
								imageSizeScaled.x,
								imageSizeScaled.y,
								BufferedImage.TYPE_INT_ARGB
							)
						);
						drawPanel.setImage();
						_displayPaint.setMask(drawPanel.getMask());
						_displayPaint.setImageSizeScaled();
						setZoomMax();
					}
					catch (IOException | OutOfMemoryError error)
					{
						drawPanel.resetImage();
						_displayPaint.resetImage();
						_paintHelper.paintImage = null;
						_controlBuilder.showError
						(
							controlPaint, "Cannot load Image, file is probably too large", error
						);
					}
					_displayPaint.repaint();
					drawPanel.repaint();
					drawPanel.setIsImageLoading(false);
				}
			};
			folderLoadingThread.start();
		}
	}

	private void setFile(File file)
	{
		drawPanel.setIsImageLoading(true);
		_fileHelper.paintFolder = file;
		var controlPaint = this;
		var fileLoadingThread = new Thread("fileLoadingThread")
		{
			public void run()
			{
				try
				{
					_paintHelper.paintImage = null;
					_paintHelper.paintImage = new ImageWrapper(ImageIO.read(file));
					_paintHelper.paintControlImage = _paintHelper.paintImage;
					if (_paintHelper.paintImage != null)
					{
						drawPanel.setImage();
						_displayPaint.setMask(drawPanel.getMask());
						_displayPaint.setImageSizeScaled();
						setZoomMax();
					}
				}
				catch (IOException | OutOfMemoryError error)
				{
					drawPanel.resetImage();
					_displayPaint.resetImage();
					_paintHelper.paintImage = null;
					_controlBuilder.showError(controlPaint, "Cannot load Image, file is probably too large", error);
				}
				_displayPaint.repaint();
				drawPanel.repaint();
				drawPanel.setIsImageLoading(false);
			}
		};
		fileLoadingThread.start();
	}
	
	private void setZoomMax()
	{
		// Image cannot be smaller than the screen.
		var paintImageSize = _paintHelper.paintImage.size();
		var displaySize = _paintHelper.displaySize;
		var w = paintImageSize.x / displaySize.x;
		var h = paintImageSize.y / displaySize.y;
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
