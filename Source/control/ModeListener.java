package control;

import java.awt.event.*;
import main.*;

public class ModeListener implements ActionListener
{
	private final WindowType displayToChange;
	private final Mode modeToChange;
	
	private Main _main = Main.Instance;
	
	public ModeListener(WindowType displayToChange, Mode modeToChange)
	{
		this.displayToChange = displayToChange;
		this.modeToChange = modeToChange;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		_main.changeButton(displayToChange, modeToChange);
	}
}
