package control;

import java.awt.*;
import javax.swing.*;
import main.*;

public class ControlWindow extends JFrame // todo - Should this inherit from Control?
{
	private JButton[] controlButtons;
	private JButton[] displayButtons;
	
	private Main _main = Main.Instance;
	protected Settings _settings;

	public ControlWindow(Settings settings, Rectangle r)
	{
		_settings = settings;
		setIconImage(_settings.icons.Program.getImage());
		setTitle(_settings.NAME);
		var controlSize = _settings.CONTROL_SIZE;
		setSize(controlSize);
		setLocation
		(
			(r.width - controlSize.width) / 2 + r.x,
			(r.height - controlSize.height) / 2 + r.y
		);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		var colors = _settings.colors;
		var controlBuilder = _settings.controlBuilder;
		var modes = Mode.values();
		var modeCount = modes.length;
		controlButtons = new JButton[modeCount];
		displayButtons = new JButton[modeCount];
		for (var i = 0; i < controlButtons.length; i++) 
		{
			var mode = modes[i];
			var modeName = mode.name();
			
			var controlButton = controlBuilder.createButton
			(
				modeName, colors.INACTIVE, new ModeListener(WindowType.CONTROL, mode)
			);
			controlButtons[i] = controlButton;

			var displayButton = controlBuilder.createButton
			(
				modeName, colors.INACTIVE, new ModeListener(WindowType.DISPLAY, mode)
			);
			displayButtons[i] = displayButton;
		}
		
		var northPanel = controlBuilder.createPanel(new GridLayout(1, 2));
		northPanel.add(createButtonGroup("Controls", controlButtons));
		northPanel.add(createButtonGroup("Displaying", displayButtons));
		
		add(northPanel, BorderLayout.NORTH);
	}
	
	private JPanel createButtonGroup(String title, JButton[] buttons)
	{
		var controlBuilder = _settings.controlBuilder;
		var colors = _settings.colors;
		
		var colorBackground = colors.BACKGROUND;
		var colorControlBackground = colors.CONTROL_BACKGROUND;

		var panel = controlBuilder.createPanel
		(
			new GridLayout(2, 1), colorControlBackground
		);
		panel.setBorder(BorderFactory.createLineBorder(colorBackground, 2));
		
		var south = controlBuilder.createPanel
		(
			new GridLayout(1, buttons.length), colorControlBackground
		);
		
		for (var button : buttons)
		{
			south.add(button);
		}
		
		panel.add(controlBuilder.createLabel(title, SwingConstants.CENTER));
		panel.add(south);
		
		return panel;
	}
	
	public void setMode(Mode newMode, Mode oldMode)
	{
		remove(_main.getControl(oldMode));
		_main.getControl(oldMode).setIsMainControl(false);
		add(_main.getControl(newMode), BorderLayout.CENTER);
		_main.getControl(newMode).setIsMainControl(true);
		validate();
		repaint();
	}
	
	public void setButton(WindowType display, Mode mode, boolean value)
	{
		var colors = _settings.colors;

		if (display == WindowType.CONTROL)
		{
			controlButtons[mode.ordinal()].setBackground(value ? colors.ACTIVE : colors.INACTIVE);
		}
		else if (display == WindowType.DISPLAY)
		{
			displayButtons[mode.ordinal()].setBackground(value ? colors.ACTIVE : colors.INACTIVE);
		}
	}
}
