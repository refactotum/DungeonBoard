package control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import main.*;

public abstract class Control extends JPanel
{
	protected Main _main = Main.Instance;
	private Settings _settings = Settings.Instance;
	protected ControlBuilder _controlBuilder = ControlBuilder.Instance;
	protected Settings.ErrorHelper _errorHelper = _settings.errorHelper;
	protected Settings.FileHelper _fileHelper = _settings.fileHelper;
	protected Settings.Icons _icons = _settings.icons;
	protected Settings.PaintHelper _paintHelper = _settings.paintHelper;

	public Control()
	{
		setLayout(new BorderLayout());
		var border =
			BorderFactory.createLineBorder(_controlBuilder.colors.BACKGROUND, 5);
		setBorder(border);
	}

	protected JPanel getNorthPanel()
	{
		var northPanel = getEmptyNorthPanel();
		var refreshButton = _controlBuilder.createButton
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
		var northPanel = _controlBuilder.createPanelWithBoxLayout
		(
			BoxLayout.X_AXIS, _controlBuilder.colors.CONTROL_BACKGROUND
		);
		northPanel.repaint();
		return northPanel;
	}

	protected abstract void load();

	public void setIsMainControl(boolean value)
	{
		// Do nothing.
	}
}
