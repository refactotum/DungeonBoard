package display;

import java.awt.*;
import javax.swing.*;
import main.*;

public abstract class Display extends JPanel
{
	protected Settings _settings = Settings.Instance;
	protected Main _main = Main.Instance;

	protected void paintMouse(Graphics2D g2d)
	{
		_main.getDisplay().paintDisplay(g2d);
	}

	public void setIsMainDisplay(boolean value)
	{
		// Do nothing.
	}
}
