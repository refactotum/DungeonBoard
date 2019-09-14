package control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import main.*;

public abstract class Control extends JPanel
{
	protected Main _main = Main.Instance;
	protected Settings _settings;

	public Control(Settings settings)
	{
		this._settings = settings;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createLineBorder(_settings.colors.BACKGROUND, 5));
	}
	
	protected JPanel getNorthPanel()
	{
		var northPanel = getEmptyNorthPanel();
		var refreshButton = _settings.controlBuilder.createButton
		(
			_settings.icons.Refresh,
			e -> { load(); } 
		);
		northPanel.add(refreshButton);
		northPanel.repaint();

		return northPanel;
	}
	
	protected JPanel getEmptyNorthPanel()
	{
		var northPanel = new JPanel();
		northPanel.setBackground(_settings.colors.CONTROL_BACKGROUND);
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
		northPanel.repaint();
		return northPanel;
	}
	
	protected abstract void load();

	public void setIsMainControl(boolean value)
	{
		// Do nothing.
	}
}
