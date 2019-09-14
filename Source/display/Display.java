package display;

import java.awt.*;
import javax.swing.*;
import main.*;

public abstract class Display extends JPanel
{
	protected Settings _settings = Settings.Instance;
	protected ControlBuilder _controlBuilder = ControlBuilder.Instance;
	protected Settings.PaintHelper _paintHelper = _settings.paintHelper;

	protected Main _main = Main.Instance;
	
	public Display()
	{}

	protected void paintMouse(Graphics2D g2d)
	{
		_main.getDisplay().paintDisplay(g2d);
	}

	public void setIsMainDisplay(boolean value)
	{
		// Do nothing.
	}
}
