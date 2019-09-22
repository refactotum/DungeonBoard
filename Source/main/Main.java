package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import control.*;
import display.*;

public class Main
{
	public static Main Instance = new Main();

	public static String ApplicationName = "Dungeon Board";

	private Mode _controlMode = Mode.Paint;
	private ControlWindow _controlWindow;
	private ControlPictures _controlLayer;
	private ControlPictures _controlImage;
	private ControlPaint _controlPaint;
	private ControlLoading _controlLoading;

	private Mode _displayMode = Mode.Loading;
	private DisplayWindow _displayWindow;
	private DisplayPictures _displayLayer;
	private DisplayPictures _displayImage;
	public DisplayPaint displayPaint;
	public DisplayLoading displayLoading;

	private ControlBuilder _controlBuilder = ControlBuilder.Instance;
	private FileHelper _fileHelper = FileHelper.Instance;
	private PaintHelper _paintHelper = PaintHelper.Instance;

	public static void main(String[] args)
	{
		Main.Instance.run();
	}

	private void run()
	{
		initializeLookAndFeel();
		loadAndInitializeScreens();
	}
	
	private void initializeLookAndFeel()
	{
		try
		{
			var colors = _controlBuilder.colors;
			var colorBackground = colors.background;
			var colorControlBackground = colors.controlBackground;
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			UIManager.put("Button.background", colorControlBackground);
			UIManager.put("Button.opaque", true);
			UIManager.put("OptionPane.background", colorBackground);
			UIManager.put("Panel.background", colorBackground);
			UIManager.put("Slider.background", colorControlBackground);
		}
		catch 
		(
			ClassNotFoundException
			| InstantiationException
			| IllegalAccessException
			| UnsupportedLookAndFeelException e
		)
		{
			_controlBuilder.showError(this.getControl(), "Error - Changing look and feel", e);
		}
	}
	
	private void loadAndInitializeScreens()
	{
		try
		{
			_fileHelper.load();
			var screens = getScreens();
			var displayIndex = _controlBuilder.getInputFromOptionDialog
			(
				"Select Display Window", Main.ApplicationName, screens
			);

			if (displayIndex >= 0 && displayIndex < screens.length)
			{
				var controlIndex = (displayIndex == 0 ? screens.length - 1 : 0);
				var screen = screens[displayIndex];
				var screenControl = screens[controlIndex];
				initializeForScreens(screen, screenControl);
			}
		}
		catch (SecurityException e)
		{
			_controlBuilder.showError(this.getControl(), "Error - Loading resources", e);
		}
		catch (HeadlessException e)
		{
			System.out.println("Error - Cannot find any screens\n" + e.getMessage());
		}
	}

	private void initializeForScreens(Screen screen, Screen screenControl)
	{
		_paintHelper.displaySize = screen.getSize();

		var folders = _fileHelper.folders;
		var folderLayer = folders[Mode.Layer.ordinal()];
		var folderImage = folders[Mode.Image.ordinal()];

		_displayWindow = new DisplayWindow(screen.getRectangle());
		_displayLayer = new DisplayPictures(folderLayer);
		_displayImage = new DisplayPictures(folderImage);
		displayPaint = new DisplayPaint();
		displayLoading = new DisplayLoading();

		_controlWindow = new ControlWindow
		(
			Main.ApplicationName,
			_paintHelper.icons.Program.getImage(),
			screenControl.getRectangle()
		);
		_controlLayer = new ControlPictures(folderLayer, _displayLayer, true);
		_controlImage = new ControlPictures(folderImage, _displayImage, false);
		_controlPaint = new ControlPaint(displayPaint);
		_controlLoading = new ControlLoading(displayLoading, _displayWindow);

		_controlWindow.addWindowListener
		(
			new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent windowEvent)
				{
					_controlPaint.saveMask();
				}
			}
		);

		_controlWindow.setButton(WindowType.Control, Mode.Paint, true);
		_controlWindow.setButton(WindowType.Display, Mode.Loading, true);
		_controlWindow.setControl
		(
			this.getControlForMode(_controlMode),
			this.getControlForMode(Mode.Image)
		);
		_displayWindow.setDisplay
		(
			this.getDisplayForMode(_displayMode), 
			this.getDisplayForMode(Mode.Image)
		);

		synchronized (_controlWindow)
		{
			_displayWindow.setVisible(true);
			_controlWindow.setVisible(true);
		}
	}

	public Control getControlForMode(Mode mode)
	{
		Control returnValue = null;

		if (mode == Mode.Image)
		{
			returnValue = _controlImage;
		}
		else if (mode == Mode.Layer)
		{
			returnValue = _controlLayer;
		}
		else if (mode == Mode.Loading)
		{
			returnValue = _controlLoading;
		}
		else if (mode == Mode.Paint)
		{
			returnValue = _controlPaint;
		}

		return returnValue;
	}

	public Display getDisplayForMode(Mode mode)
	{
		Display returnValue = null;

		if (mode == Mode.Image)
		{
			returnValue = _displayImage;
		}
		else if (mode == Mode.Layer)
		{
			returnValue = _displayLayer;
		}
		else if (mode == Mode.Loading)
		{
			returnValue = displayLoading;
		}
		else if (mode == Mode.Paint)
		{
			returnValue = displayPaint;
		}

		return returnValue;
	}

	private Screen[] getScreens() throws HeadlessException
	{
		var graphicsDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		var screens = new Screen[graphicsDevices.length];
		for (var i = 0; i < graphicsDevices.length; i++)
		{
			screens[i] = new Screen(graphicsDevices[i]);
		}
		return screens;
	}

	public DisplayWindow getDisplay()
	{
		return _displayWindow;
	}

	public ControlWindow getControl()
	{
		return _controlWindow;
	}

	public void changeButton(WindowType disp, Mode mode)
	{
		if (disp == WindowType.Control)
		{
			synchronized (_controlMode)
			{
				if (_controlMode != mode) {
					_controlWindow.setButton(disp, _controlMode, false);
					_controlWindow.setControl
					(
						this.getControlForMode(mode),
						this.getControlForMode(_controlMode)
					);
					_controlMode = mode;
					_controlWindow.setButton(disp, _controlMode, true);
				}
			}
		}
		else if (disp == WindowType.Display)
		{
			synchronized (_displayMode)
			{
				if (_displayMode != mode)
				{
					_controlWindow.setButton(disp, _displayMode, false);
					_displayWindow.setDisplay
					(
						this.getDisplayForMode(mode), 
						this.getDisplayForMode(_displayMode)
					);
					_displayMode = mode;
					_controlWindow.setButton(disp, _displayMode, true);
				}
			}
		}
	}
}
