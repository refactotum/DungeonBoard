package display;

import java.awt.*;

import common.*;
import main.*;

public class DisplayTimer // todo - Should this inherit Display?
{
	private final int _timerWidth = 220;
	private final int _timerWidthMult = 60;
	private final int _timerHeight = 90;
	private final int _secondsToShowCompletedTimer = 60;
	private final Font _font = new Font("TimesRoman", Font.BOLD, 120);

	private int timerSecondsRemaining;
	private Thread paintThread;
	private final int left;
	private final int top;
	private final int bottom;
	
	private Main _main = Main.Instance;

	public DisplayTimer(Coords size)
	{
		left = (size.x - _timerWidth) / 2;
		top = size.y - _timerHeight;
		bottom = size.y - 4;
		timerSecondsRemaining = -_secondsToShowCompletedTimer;
		paintThread = new Thread();
	}
	
	public void paint(Graphics2D g2d)
	{
		if (isTimerEnabled())
		{
			g2d.setFont(_font);
			if (timerSecondsRemaining <= 0)
			{
				paintTimer(g2d, Color.RED, 0);
			}
			else
			{
				paintTimer(g2d, Color.LIGHT_GRAY, timerSecondsRemaining);
			}
		}
	}
	
	private void paintTimer(Graphics2D g2d, Color background, int seconds)
	{
		var digits = Math.max((int)Math.log10(seconds / 60), 0);
		var x = left - _timerWidthMult * digits / 2;
		var w = _timerWidth + _timerWidthMult * digits;
		g2d.setColor(background);
		g2d.fillRoundRect(x, top, w, _timerHeight, 15, 15);
		g2d.setColor(Color.BLACK);
		g2d.drawString
		(
			String.format("%d:%02d", (int)(seconds / 60), seconds % 60), x, bottom
		);
	}
	
	public boolean isTimerEnabled()
	{
		return timerSecondsRemaining > -_secondsToShowCompletedTimer;
	}

	public void setTimerSecondsRemaining(int seconds)
	{
		clearTimer();
		timerSecondsRemaining = seconds + 1;
		paintThread = new Thread("paintThread")
		{
			@Override
			public void run()
			{
				while (isTimerEnabled())
				{
					try
					{
						timerSecondsRemaining--;
						_main.getDisplay().repaint();
						sleep(1000);
					}
					catch (InterruptedException e)
					{
						break;
					}
				}
			}
		};
		paintThread.start();
	}
	
	public void clearTimer()
	{
		timerSecondsRemaining = -_secondsToShowCompletedTimer;
		paintThread.interrupt();
		try
		{
			paintThread.join();
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}
		_main.getDisplay().repaint();
	}
}
