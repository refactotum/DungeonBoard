package display;

import java.awt.*;
import javax.swing.*;
import main.*;

public abstract class Display extends JPanel
{
	protected Settings _settings;
	protected Main _main = Main.Instance;
	
	public Display(Settings settings)
	{
		_settings = settings;
	}

	protected void paintMouse(Graphics2D g2d)
	{
		_main.getDisplay().paintDisplay(g2d);
	}

	public void setIsMainDisplay(boolean value)
	{
		// Do nothing.
	}
}
