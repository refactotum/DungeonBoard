package control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import main.*;

public abstract class Control extends JPanel
{
	protected ControlBuilder _controlBuilder = ControlBuilder.Instance;
	protected FileHelper _fileHelper = FileHelper.Instance;
	protected PaintHelper _paintHelper = PaintHelper.Instance;

	public Control()
	{
		setLayout(new BorderLayout());
		var border =
			BorderFactory.createLineBorder(_controlBuilder.colors.background, 5);
		setBorder(border);
	}

	protected JPanel getNorthPanel()
	{
		var northPanel = getEmptyNorthPanel();
		var refreshButton = _controlBuilder.createButton
		(
			_paintHelper.icons.Refresh,
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
			BoxLayout.X_AXIS, _controlBuilder.colors.controlBackground
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
