package control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import main.*;

public class ControlLoading extends Control
{
	public ControlLoading(Settings settings)
	{
		super(settings);
		var northPanel = getNorthPanel();

		var colors = _settings.colors;

		var controlBuilder = _settings.controlBuilder;
		var upScaleButton = controlBuilder.createButton
		(
			"Up Scale", colors.INACTIVE,
			e ->
			{
				var button = ((JButton)(e.getSource()));
				var upScaleButtonBackground = button.getBackground();
				if (upScaleButtonBackground == colors.ACTIVE)
				{
					_main.DISPLAY_LOADING.setShouldImagesBeUpscaled(false);
					button.setBackground(colors.INACTIVE);
				}
				else if (upScaleButtonBackground == colors.INACTIVE)
				{
					_main.DISPLAY_LOADING.setShouldImagesBeUpscaled(true);
					button.setBackground(colors.ACTIVE);
				}
			}
		);
		northPanel.add(upScaleButton);
		
		var addCubeButton = controlBuilder.createButton
		(
			"Add Cube",
			e ->
			{
				_main.DISPLAY_LOADING.addScreensaverCube();
			}
		);
		northPanel.add(addCubeButton);
		
		var clearScreensaverCubeButton = controlBuilder.createButton
		(
			"Clear Screensaver Cubes",
			e ->
			{
				_main.DISPLAY_LOADING.clearScreensaverCubes();
			}
		);
		northPanel.add(clearScreensaverCubeButton);

		var colorControlBackground = colors.CONTROL_BACKGROUND;

		var timeLabel = controlBuilder.createLabel
		(
			"08", SwingConstants.CENTER, colorControlBackground
		);
		northPanel.add(timeLabel);
		
		var timeSlider = controlBuilder.createSlider
		(
			SwingConstants.HORIZONTAL, 1, 20, 8,
			new Dimension(100, 0),
			colorControlBackground,
			e ->
			{
				var slider = ((JSlider)(e.getSource()));
				var sliderValue = slider.getValue();
				timeLabel.setText(String.format("%02d", sliderValue));
				_main.DISPLAY_LOADING.setSecondsPerImage(sliderValue);
			}
		);
		northPanel.add(timeSlider);
		
		var createTimerButton = controlBuilder.createButton
		(
			"Create Timer",
			e ->
			{
				var input = JOptionPane.showInputDialog(_main.getControl(), "Enter minutes or M:SS", "");
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
					_main.getDisplay().setTimerSecondsRemaining(seconds);
				}
				catch (NumberFormatException | NullPointerException e2)
				{
					// Do nothing.
				}
			}
		);
		northPanel.add(createTimerButton);
		
		var clearTimerButton = controlBuilder.createButton
		(
			"Clear Timer",
			e -> { _main.getDisplay().clearTimer(); }
		);
		northPanel.add(clearTimerButton);
		
		add(northPanel, BorderLayout.NORTH);
		
		setVisible(true);
	}

	@Override
	protected void load()
	{
		// Do nothing.
	}
}
