package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import control.*;
import display.*;

public class Main
{
	public static Main Instance = new Main();

	private ControlWindow _controlWindow;
	private ControlPictures _controlLayer;
	private ControlPictures _controlImage;
	private ControlPaint _controlPaint;
	private ControlLoading _controlLoading;
	private Mode _controlMode = Mode.PAINT;
	private DisplayWindow _displayWindow;
	private DisplayPictures _displayLayer;
	private DisplayPictures _displayImage;
	private Mode _displayMode = Mode.LOADING;
	
	public DisplayPaint DISPLAY_PAINT;
	public DisplayLoading DISPLAY_LOADING;
	
	private Settings _settings = Settings.Instance;

	public static void main(String[] args)
	{
		Main.Instance.run();
	}
	
	private void run()
	{
		try
		{
			var colors = _settings.colors;
			var colorBackground = colors.BACKGROUND;
			var colorControlBackground = colors.CONTROL_BACKGROUND;
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
			_settings.showError("Error - Changing look and feel", e);
		}
		
		try
		{
			_settings.load();
			var screens = getScreens();
			var displayIndex = JOptionPane.showOptionDialog
			(
				null, "Select Display Window", _settings.NAME,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null, screens, 0
			);
			
			if (displayIndex >= 0 && displayIndex < screens.length)
			{
				var controlIndex = (displayIndex == 0 ? screens.length - 1 : 0);
				
				_settings.DISPLAY_SIZE = screens[displayIndex].getSize();
				
				_displayWindow = new DisplayWindow(screens[displayIndex].getRectangle());
				
				var controlWindow = new ControlWindow(screens[controlIndex].getRectangle());
				_controlWindow = controlWindow;
				
				var folders = _settings.directories.FOLDERS;
				_displayLayer = new DisplayPictures(folders[Mode.LAYER.ordinal()]);
				_displayImage = new DisplayPictures(folders[Mode.IMAGE.ordinal()]);
				DISPLAY_PAINT = new DisplayPaint();
				DISPLAY_LOADING = new DisplayLoading();
				
				_controlLayer = new ControlPictures(folders[Mode.LAYER.ordinal()], _displayLayer, true);
				_controlImage = new ControlPictures(folders[Mode.IMAGE.ordinal()], _displayImage, false);
				_controlPaint = new ControlPaint();
				_controlLoading = new ControlLoading();
				
				controlWindow.addWindowListener
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
				
				controlWindow.setButton(WindowType.CONTROL, Mode.PAINT, true);
				controlWindow.setButton(WindowType.DISPLAY, Mode.LOADING, true);
				controlWindow.setMode(_controlMode, Mode.IMAGE);
				_displayWindow.setMode(_displayMode, Mode.IMAGE);
				
				synchronized (controlWindow)
				{
					_displayWindow.setVisible(true);
					controlWindow.setVisible(true);
				}
			}
		}
		catch (SecurityException e)
		{
			_settings.showError("Error - Loading resources", e);
		}
		catch (HeadlessException e)
		{
			System.out.println("Error - Cannot find any screens\n" + e.getMessage());
		}
	}
	
	public Control getControl(Mode mode)
	{
		Control returnValue = null;

		if (mode == Mode.IMAGE)
		{
			returnValue = _controlImage;
		}
		else if (mode == Mode.LAYER)
		{
			returnValue = _controlLayer;
		}
		else if (mode == Mode.LOADING)
		{
			returnValue = _controlLoading;
		}
		else if (mode == Mode.PAINT)
		{
			returnValue = _controlPaint;
		}

		return returnValue;
	}

	public Display getDisplay(Mode mode)
	{
		Display returnValue = null;

		if (mode == Mode.IMAGE)
		{
			returnValue = _displayImage;
		}
		else if (mode == Mode.LAYER)
		{
			returnValue = _displayLayer;
		}
		else if (mode == Mode.LOADING)
		{
			returnValue = DISPLAY_LOADING;
		}
		else if (mode == Mode.PAINT)
		{
			returnValue = DISPLAY_PAINT;
		}

		return returnValue;
	}

	private Screen[] getScreens() throws HeadlessException
	{
		var graphicsDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		var screens = new Screen[graphicsDevices.length];
		for (int i = 0; i < graphicsDevices.length; i++)
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
		if (disp == WindowType.CONTROL)
		{
			synchronized (_controlMode)
			{
				if (_controlMode != mode) {
					_controlWindow.setButton(disp, _controlMode, false);
					_controlWindow.setMode(mode, _controlMode);
					_controlMode = mode;
					_controlWindow.setButton(disp, _controlMode, true);
				}
			}
		}
		else if (disp == WindowType.DISPLAY)
		{
			synchronized (_displayMode)
			{
				if (_displayMode != mode)
				{
					_controlWindow.setButton(disp, _displayMode, false);
					_displayWindow.setMode(mode, _displayMode);
					_displayMode = mode;
					_controlWindow.setButton(disp, _displayMode, true);
				}
			}
		}
	}
}
