package control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import main.*;

public class ControlLoading extends Control
{
	public ControlLoading()
	{
		super();
		var northPanel = getNorthPanel();

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
					_main.displayLoading.setShouldImagesBeUpscaled(false);
					button.setBackground(colors.inactive);
				}
				else if (upScaleButtonBackground == colors.inactive)
				{
					_main.displayLoading.setShouldImagesBeUpscaled(true);
					button.setBackground(colors.active);
				}
			}
		);
		northPanel.add(upScaleButton);
		
		var addCubeButton = _controlBuilder.createButton
		(
			"Add Cube",
			e ->
			{
				_main.displayLoading.addScreensaverCube();
			}
		);
		northPanel.add(addCubeButton);
		
		var clearScreensaverCubeButton = _controlBuilder.createButton
		(
			"Clear Screensaver Cubes",
			e ->
			{
				_main.displayLoading.clearScreensaverCubes();
			}
		);
		northPanel.add(clearScreensaverCubeButton);

		var colorControlBackground = colors.controlBackground;

		var timeLabel = _controlBuilder.createLabel
		(
			"08", SwingConstants.CENTER, colorControlBackground
		);
		northPanel.add(timeLabel);

		var timeSlider = _controlBuilder.createSlider
		(
			SwingConstants.HORIZONTAL, 1, 20, 8,
			new Dimension(100, 0),
			colorControlBackground,
			e ->
			{
				var slider = ((JSlider)(e.getSource()));
				var sliderValue = slider.getValue();
				timeLabel.setText(String.format("%02d", sliderValue));
				_main.displayLoading.setSecondsPerImage(sliderValue);
			}
		);
		northPanel.add(timeSlider);
		
		var createTimerButton = _controlBuilder.createButton
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
		
		var clearTimerButton = _controlBuilder.createButton
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
