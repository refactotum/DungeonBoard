package display;

import java.awt.*;
import javax.swing.*;
import main.*;

public abstract class Display extends JPanel
{
	protected ControlBuilder _controlBuilder = ControlBuilder.Instance;
	protected FileHelper _fileHelper = FileHelper.Instance;
	protected Icons _icons = Icons.Instance;
	protected PaintHelper _paintHelper = PaintHelper.Instance;

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
