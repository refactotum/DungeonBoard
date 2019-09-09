package control;

import java.awt.*;
import javax.swing.*;
import main.*;

public class ControlWindow extends JFrame // todo - Should this inherit from Control?
{
	private JButton[] controlButtons;
	private JButton[] displayButtons;
	
	private Main _main = Main.Instance;
	private Settings _settings = Settings.Instance;

	public ControlWindow(Rectangle r)
	{
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
		var modes = Mode.values();
		var modeCount = modes.length;
		controlButtons = new JButton[modeCount];
		displayButtons = new JButton[modeCount];
		for (var i = 0; i < controlButtons.length; i++) 
		{
			var mode = modes[i];
			
			var controlButton = _settings.createButton(mode.name());
			controlButton.setBackground(colors.INACTIVE);
			controlButton.addActionListener(new ModeListener(WindowType.CONTROL, mode));
			controlButtons[i] = controlButton;

			var displayButton = _settings.createButton(mode.name());
			displayButton.setBackground(colors.INACTIVE);
			displayButton.addActionListener(new ModeListener(WindowType.DISPLAY, mode));
			displayButtons[i] = displayButton;
		}
		
		var northPanel = new JPanel(new GridLayout(1, 2));
		northPanel.add(createButtonGroup("Controls", controlButtons));
		northPanel.add(createButtonGroup("Displaying", displayButtons));
		
		add(northPanel, BorderLayout.NORTH);
	}
	
	private JPanel createButtonGroup(String title, JButton[] buttons)
	{
		var colors = _settings.colors;
		
		var colorBackground = colors.BACKGROUND;
		var colorControlBackground = colors.CONTROL_BACKGROUND;

		var panel = new JPanel();
		panel.setBackground(colorControlBackground);
		panel.setLayout(new GridLayout(2, 1));
		panel.setBorder(BorderFactory.createLineBorder(colorBackground, 2));
		
		var south = new JPanel();
		south.setBackground(colorControlBackground);
		south.setLayout(new GridLayout(1, buttons.length));
		
		for (var button : buttons)
		{
			south.add(button);
		}
		
		panel.add(new JLabel(title, SwingConstants.CENTER));
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
