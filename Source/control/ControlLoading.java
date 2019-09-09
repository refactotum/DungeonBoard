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
		var northPanel = getNorthPanel();

		var colors = _settings.colors;

		var upScaleButton = _settings.createButton("Up Scale");
		upScaleButton.setBackground(colors.INACTIVE);
		upScaleButton.addActionListener
		(
			arg0 ->
			{
				var upScaleButtonBackground = upScaleButton.getBackground();
				if (upScaleButtonBackground == colors.ACTIVE)
				{
					_main.DISPLAY_LOADING.setShouldImagesBeUpscaled(false);
					upScaleButton.setBackground(colors.INACTIVE);
				}
				else if (upScaleButtonBackground == colors.INACTIVE)
				{
					_main.DISPLAY_LOADING.setShouldImagesBeUpscaled(true);
					upScaleButton.setBackground(colors.ACTIVE);
				}
			}
		);
		northPanel.add(upScaleButton);
		
		var addCubeButton = _settings.createButton("Add Cube");
		addCubeButton.addActionListener
		(
			e ->
			{
				_main.DISPLAY_LOADING.addScreensaverCube();
			}
		);
		northPanel.add(addCubeButton);
		
		var clearScreensaverCubeButton = _settings.createButton("Clear Screensaver Cubes");
		clearScreensaverCubeButton.addActionListener
		(
			e ->
			{
				_main.DISPLAY_LOADING.clearScreensaverCubes();
			}
		);
		northPanel.add(clearScreensaverCubeButton);

		var colorControlBackground = colors.CONTROL_BACKGROUND;

		var timeLabel = new JLabel("08");
		timeLabel.setBackground(colorControlBackground);
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		northPanel.add(timeLabel);
		
		var timeSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 20, 8);
		timeSlider.setBackground(colorControlBackground);
		timeSlider.setMinimumSize(new Dimension(100, 0));
		timeSlider.addChangeListener
		(
			e ->
			{
				timeLabel.setText(String.format("%02d", timeSlider.getValue()));
				_main.DISPLAY_LOADING.setSecondsPerImage(timeSlider.getValue());
			}
		);
		northPanel.add(timeSlider);
		
		var createTimerButton = _settings.createButton("Create Timer");
		createTimerButton.addActionListener
		(
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
		
		var clearTimerButton = _settings.createButton("Clear Timer");
		clearTimerButton.addActionListener
		(
			new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					_main.getDisplay().clearTimer();
				}
			}
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
