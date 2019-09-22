package control;

import java.awt.*;
import javax.swing.*;
import main.*;

public class ControlWindow extends JFrame // todo - Should this inherit from Control?
{
	private JButton[] controlButtons;
	private JButton[] displayButtons;
	
	protected ControlBuilder _controlBuilder = ControlBuilder.Instance;

	public ControlWindow(String name, Image iconImage, Rectangle r)
	{
		setTitle(name);
		setIconImage(iconImage);

		var controlSize = _controlBuilder.controlSize;
		setSize(controlSize.toDimension());
		setLocation
		(
			(r.x - controlSize.x) / 2 + r.x,
			(r.y - controlSize.y) / 2 + r.y
		);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		var northPanel = createNorthPanel();
		add(northPanel, BorderLayout.NORTH);
	}
	
	private JPanel createNorthPanel()
	{
		var northPanel = _controlBuilder.createPanel(new GridLayout(1, 2));
		this.controlButtons = createControlButtons();
		this.displayButtons = createDisplayButtons();
		northPanel.add(createButtonGroup("Controls", this.controlButtons));
		northPanel.add(createButtonGroup("Displaying", this.displayButtons));
		return northPanel;
	}
	
	private JButton[] createControlButtons()
	{
		var modes = Mode.values();
		var modeCount = modes.length;
		var colors = _controlBuilder.colors;

		var controlButtons = new JButton[modeCount];
		for (var i = 0; i < modeCount; i++) 
		{
			var mode = modes[i];
			var modeName = mode.name();
			var controlButton = _controlBuilder.createButton
			(
				modeName, colors.inactive, new ModeListener(WindowType.Control, mode)
			);
			controlButtons[i] = controlButton;
		}
		
		return controlButtons;
	}
	
	private JButton[] createDisplayButtons()
	{
		var modes = Mode.values();
		var modeCount = modes.length;
		var colors = _controlBuilder.colors;

		displayButtons = new JButton[modeCount];
		for (var i = 0; i < modeCount; i++) 
		{
			var mode = modes[i];
			var modeName = mode.name();
			var displayButton = _controlBuilder.createButton
			(
				modeName, colors.inactive, new ModeListener(WindowType.Display, mode)
			);
			displayButtons[i] = displayButton;
		}
		return displayButtons;
	}

	private JPanel createButtonGroup(String title, JButton[] buttons)
	{
		var controlBuilder = _controlBuilder;
		var colors = controlBuilder.colors;
		
		var colorBackground = colors.background;
		var colorControlBackground = colors.controlBackground;

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

	public void setControl(Control controlNew, Control controlOld)
	{
		remove(controlOld);
		controlOld.setIsMainControl(false);
		add(controlNew, BorderLayout.CENTER);
		controlNew.setIsMainControl(true);
		validate();
		repaint();
	}
	
	public void setButton(WindowType display, Mode mode, boolean value)
	{
		var colors = _controlBuilder.colors;

		if (display == WindowType.Control)
		{
			controlButtons[mode.ordinal()].setBackground(value ? colors.active : colors.inactive);
		}
		else if (display == WindowType.Display)
		{
			displayButtons[mode.ordinal()].setBackground(value ? colors.active : colors.inactive);
		}
	}
}
