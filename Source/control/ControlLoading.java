package control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import common.*;
import display.*;

public class ControlLoading extends Control
{
	private DisplayLoading _displayLoading;
	private DisplayWindow _displayWindow;

	public ControlLoading(DisplayLoading displayLoading, DisplayWindow displayWindow)
	{
		super();
		this._displayLoading = displayLoading;
		this._displayWindow = displayWindow;

		var northPanel = getNorthPanel();

		northPanel.add(createUpScaleButton());
		northPanel.add(createAddScreensaverCubeButton());
		northPanel.add(createClearScreensaverCubeButton());
		var timeLabel = createTimeLabel();
		northPanel.add(timeLabel);
		northPanel.add(createTimeSlider(timeLabel));
		northPanel.add(createCreateTimerButton());
		northPanel.add(createClearTimerButton());

		add(northPanel, BorderLayout.NORTH);
		setVisible(true);
	}
	
	private JButton createUpScaleButton()
	{
		var colors = _controlBuilder.colors;

		var upScaleButton = _controlBuilder.createButton
		(
			"Up Scale", colors.inactive,
			e ->
			{
				var button = ((JButton)(e.getSource()));
				var upScaleButtonBackground = button.getBackground();
				if (upScaleButtonBackground == colors.active)
				{
					_displayLoading.setShouldImagesBeUpscaled(false);
					button.setBackground(colors.inactive);
				}
				else if (upScaleButtonBackground == colors.inactive)
				{
					_displayLoading.setShouldImagesBeUpscaled(true);
					button.setBackground(colors.active);
				}
			}
		);
		
		return upScaleButton;
	}
	
	private JButton createAddScreensaverCubeButton()
	{
		var addCubeButton = _controlBuilder.createButton
		(
			"Add Cube",
			e -> { _displayLoading.addScreensaverCube(); }
		);
		return addCubeButton;
	}
	
	private JButton createClearScreensaverCubeButton()
	{
		var clearScreensaverCubeButton = _controlBuilder.createButton
		(
			"Clear Screensaver Cubes",
			e -> { _displayLoading.clearScreensaverCubes(); }
		);
		
		return clearScreensaverCubeButton;
	}

	private JLabel createTimeLabel()
	{
		var colorControlBackground = _controlBuilder.colors.controlBackground;

		var timeLabel = _controlBuilder.createLabel
		(
			"08", SwingConstants.CENTER, colorControlBackground
		);
		return timeLabel;
	}

	private JSlider createTimeSlider(JLabel timeLabel)
	{
		var colorControlBackground = _controlBuilder.colors.controlBackground;

		var timeSlider = _controlBuilder.createSlider
		(
			SwingConstants.HORIZONTAL, 1, 20, 8,
			new Coords(100, 0),
			colorControlBackground,
			e ->
			{
				var slider = ((JSlider)(e.getSource()));
				var sliderValue = slider.getValue();
				timeLabel.setText(String.format("%02d", sliderValue));
				_displayLoading.setSecondsPerImage(sliderValue);
			}
		);
		return timeSlider;
	}
	
	private JButton createCreateTimerButton()
	{
		var controlLoading = this;
		var createTimerButton = _controlBuilder.createButton
		(
			"Create Timer",
			e ->
			{
				var input = _controlBuilder.getInputFromTextDialog
				(
					controlLoading, "Enter minutes or M:SS"
				);
				try
				{
					var seconds = 0;
					if (input.contains(":"))
					{
						var split = input.split(":");
						seconds += Integer.parseInt(split[0]) * 60;
						seconds += Integer.parseInt(split[1]);
					}
					else
					{
						seconds += Integer.parseInt(input) * 60;
					}
					_displayWindow.setTimerSecondsRemaining(seconds);
				}
				catch (NumberFormatException | NullPointerException e2)
				{
					// Do nothing.
				}
			}
		);
		
		return createTimerButton;
	}
	
	private JButton createClearTimerButton()
	{
		var clearTimerButton = _controlBuilder.createButton
		(
			"Clear Timer",
			e -> { _displayWindow.clearTimer(); }
		);
		
		return clearTimerButton;
	}

	@Override
	protected void load()
	{
		// Do nothing.
	}
}
