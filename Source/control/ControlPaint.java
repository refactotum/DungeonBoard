package control;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
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

	public ControlPaint(Settings settings)
	{
		super(settings);

		var northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

		folderControlPanel = getEmptyNorthPanel();
		folderControlPanel.setVisible(false);

		var innerNorthPanel = getNorthPanel();

		maxZoom = 10.0;

		drawPanel = new DrawPanel();

		setFocusable(true);
		
		fileBox = new JComboBox<>();
		fileBox.setBackground(_settings.colors.CONTROL_BACKGROUND);
		fileBox.addItem("");
		load();
		fileBox.addActionListener
		(
			e ->
			{
				if (fileBox.getSelectedIndex() != 0)
				{
					var filePath =
						_settings.fileHelper.FOLDERS[Mode.PAINT.ordinal()].getAbsolutePath()
						+ File.separator + fileBox.getSelectedItem().toString();

					var file = new File(filePath);
					
					if (file.exists())
					{
						drawPanel.saveMask();
						var fileHelper = _settings.fileHelper;
						var maskFile = fileHelper.fileToMaskFile(file);
						var dataFilePath =
							fileHelper.DATA_FOLDER + File.separator 
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
								_settings.errorHelper.showError("Cannot load Mask Data", e2);
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
						_settings.errorHelper.showError("Cannot load Image, file does not exist");
					}
				}
			}
		);
		innerNorthPanel.add(fileBox);
		
		var iconsForPenDirectionLock = _settings.icons.PenDirectionLocks;
		var penDirectionLockButton = _settings.controlBuilder.createButton(iconsForPenDirectionLock[0]);
		penDirectionLockButton.addActionListener
		(
			e ->
			{
				drawPanel.togglePenDirectionLock();
				penDirectionLockButton.setIcon(iconsForPenDirectionLock[drawPanel.getStyle()]);
			}
		);
		innerNorthPanel.add(penDirectionLockButton);
		
		var controlBuilder = _settings.controlBuilder;
		
		var iconsForPenShapes = _settings.icons.PenShapes;
		var shape = controlBuilder.createButton(iconsForPenShapes[0]);
		shape.addActionListener
		(
			e ->
			{
				drawPanel.togglePenShape();
				shape.setIcon(iconsForPenShapes[drawPanel.getPenShape()]);
			}
		);
		innerNorthPanel.add(shape);
		
		var iconsForTouchpadDrawMode = _settings.icons.TouchpadDrawModes;
		var touchpadDrawModeButton = controlBuilder.createButton(iconsForTouchpadDrawMode[0]);
		touchpadDrawModeButton.addActionListener
		(
			e ->
			{
				drawPanel.toggleTouchpadDrawMode();
				touchpadDrawModeButton.setIcon(iconsForTouchpadDrawMode[drawPanel.getTouchpadDrawMode()]);
			}
		);
		innerNorthPanel.add(touchpadDrawModeButton);
		
		var colors = _settings.colors;

		var showButton = controlBuilder.createButton("Show");
		showButton.setBackground(colors.ACTIVE);
		showButton.addActionListener
		(
			e ->
			{
				drawPanel.showAll();
			}
		);
		innerNorthPanel.add(showButton);
		
		var hideButton = controlBuilder.createButton("Hide");
		hideButton.setBackground(colors.INACTIVE);
		hideButton.addActionListener
		(
			e ->
			{
				drawPanel.hideAll();
			}
		);
		innerNorthPanel.add(hideButton);
		
		var slider = new JSlider(SwingConstants.HORIZONTAL, 10, 100, 25);
		slider.setBackground(colors.CONTROL_BACKGROUND);
		slider.addChangeListener
		(
			arg0 ->
			{
				drawPanel.setPenRadius(slider.getValue());
			}
		);
		innerNorthPanel.add(slider);
		
		innerNorthPanel.add(drawPanel.getUpdateButton());
		
		var westPanel = new JPanel();
		westPanel.setBackground(colors.CONTROL_BACKGROUND);
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
		
		westPanel.add(new JLabel("Zoom", SwingConstants.LEFT));
		
		zoomText = new JTextField("1.00", 1);
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
		
		zoomSlider = new JSlider(SwingConstants.VERTICAL, 1, (int)(maxZoom * 100), 100);
		zoomSlider.addChangeListener
		(
			arg0 ->
			{
				var zoom = zoomSlider.getValue() / 100.0;
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
		
		var colors = _settings.colors;
		var controlBuilder = _settings.controlBuilder;
		
		folderControlPanel.removeAll();
		// Creates all buttons.
		for (var i = 1; i <= paintFolderSize; i++)
		{
			var button = controlBuilder.createButton(i + "");
			button.setBackground(colors.INACTIVE);
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					var number = Integer.parseInt(((JButton) e.getSource()).getText());
					
					if (button.getBackground().equals(colors.ACTIVE))
					{
						button.setBackground(colors.INACTIVE);
						paintImages[number - 1] = false;
					}
					else
					{
						button.setBackground(colors.ACTIVE);
						paintImages[number - 1] = true;
					}
					var fileLoadingThread = new Thread("fileLoadingThread")
					{
						public void run()
						{
							try
							{
								var paintImage = _settings.PAINT_IMAGE;
								synchronized (paintImage)
								{
									
									var g2d = paintImage.createGraphics();
									g2d.setColor(Color.BLACK);
									g2d.fillRect(0, 0, paintImage.getWidth(), paintImage.getHeight());
									
									var paintFolderSize = _settings.fileHelper.PAINT_FOLDER_SIZE;
									for (var i = paintFolderSize; i > 0; i--)
									{
										if (paintImages[i - 1])
										{
											var f = new File(paintFolder + "/" + i + ".png");
											g2d.drawImage(ImageIO.read(f), 0, 0, null);
										}
									}
									
									g2d.dispose();
									
									if (_settings.PAINT_IMAGE != null)
									{
										_main.DISPLAY_PAINT.setMask(drawPanel.getMask());
										_main.DISPLAY_PAINT.setImageSizeScaled();
										setZoomMax();
									}
								}
							}
							catch (IOException | OutOfMemoryError error)
							{
								drawPanel.resetImage();
								_main.DISPLAY_PAINT.resetImage();
								_settings.PAINT_IMAGE = null;
								_settings.errorHelper.showError("Cannot load Image, file is probably too large", error);
							}
							_main.DISPLAY_PAINT.repaint();
							drawPanel.repaint();
							drawPanel.setIsImageLoading(false);
						}
					};
					fileLoadingThread.start();
				}
			});
			
			folderControlPanel.add(button);
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
						_settings.PAINT_IMAGE = null;
						_settings.PAINT_CONTROL_IMAGE = null;
						Dimension imageSizeScaled;
						{
							var guideImg = ImageIO.read(guide);
							imageSizeScaled = new Dimension(guideImg.getWidth(), guideImg.getHeight());
							var paintGuideScale = _settings.PAINT_GUIDE_SCALE;
							var paintControlImage = new BufferedImage
							(
									imageSizeScaled.width / paintGuideScale,
									imageSizeScaled.height / paintGuideScale,
									BufferedImage.TYPE_INT_RGB
							);
							_settings.PAINT_CONTROL_IMAGE = paintControlImage;
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
						_settings.PAINT_IMAGE = new BufferedImage
						(
								imageSizeScaled.width,
								imageSizeScaled.height,
								BufferedImage.TYPE_INT_ARGB
							);
						drawPanel.setImage();
						_main.DISPLAY_PAINT.setMask(drawPanel.getMask());
						_main.DISPLAY_PAINT.setImageSizeScaled();
						setZoomMax();
					}
					catch (IOException | OutOfMemoryError error)
					{
						drawPanel.resetImage();
						_main.DISPLAY_PAINT.resetImage();
						_settings.PAINT_IMAGE = null;
						_settings.errorHelper.showError("Cannot load Image, file is probably too large", error);
					}
					_main.DISPLAY_PAINT.repaint();
					drawPanel.repaint();
					drawPanel.setIsImageLoading(false);
				}
			};
			folderLoadingThread.start();
		}
		
		var fileHelper = _settings.fileHelper;
		fileHelper.PAINT_FOLDER = paintFolder;
		fileHelper.PAINT_FOLDER_SIZE = paintFolderSize;
		_settings.PAINT_IMAGES = paintImages;
	}

	private void setFile(File file)
	{
		drawPanel.setIsImageLoading(true);
		_settings.fileHelper.PAINT_FOLDER = file;
		var fileLoadingThread = new Thread("fileLoadingThread")
		{
			public void run()
			{
				try
				{
					_settings.PAINT_IMAGE = null;
					_settings.PAINT_IMAGE = ImageIO.read(file);
					_settings.PAINT_CONTROL_IMAGE = _settings.PAINT_IMAGE;
					if (_settings.PAINT_IMAGE != null)
					{
						drawPanel.setImage();
						_main.DISPLAY_PAINT.setMask(drawPanel.getMask());
						_main.DISPLAY_PAINT.setImageSizeScaled();
						setZoomMax();
					}
				}
				catch (IOException | OutOfMemoryError error)
				{
					drawPanel.resetImage();
					_main.DISPLAY_PAINT.resetImage();
					_settings.PAINT_IMAGE = null;
					_settings.errorHelper.showError("Cannot load Image, file is probably too large", error);
				}
				_main.DISPLAY_PAINT.repaint();
				drawPanel.repaint();
				drawPanel.setIsImageLoading(false);
			}
		};
		fileLoadingThread.start();
	}
	
	private void setZoomMax()
	{
		// Image cannot be smaller than the screen.
		var paintImage = _settings.PAINT_IMAGE;
		var displaySize = _settings.DISPLAY_SIZE;
		var w = paintImage.getWidth() / displaySize.getWidth();
		var h = paintImage.getHeight() / displaySize.getHeight();
		maxZoom = h > w ? h : w;
		zoomSlider.setMaximum((int) (maxZoom * 100));
	}
	
	@Override
	protected void load()
	{
		var fileHelper = _settings.fileHelper;

		while (fileBox.getItemCount() > 1)
		{
			fileBox.removeItemAt(1);
		}
		var folder = fileHelper.FOLDERS[Mode.PAINT.ordinal()];
		
		if (folder.exists())
		{
			for (var f : fileHelper.listFilesInOrder(folder))
			{
				if (f.isDirectory())
				{
					fileBox.addItem(f.getName());
				}
				else
				{
					var name = f.getName();
					var suffix = name.substring(name.lastIndexOf('.') + 1);
					if (suffix.equalsIgnoreCase("PNG") || suffix.equalsIgnoreCase("JPG") || suffix.equalsIgnoreCase("JPEG"))
					{
						fileBox.addItem(name);
					}
				}
			}
		}
	}

	public void saveMask()
	{
		drawPanel.saveMask();
	}
}
