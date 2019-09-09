package control;

import java.io.*;
import java.util.*;

import javax.swing.*;

import main.*;

public class PicturePanelButtonCreator
{
	private final PicturePanel pp;
	private final LinkedList<File> queue;
	private final JButton[] buttons;
	private int queueNumber;
	
	private Settings _settings = Settings.Instance;

	public PicturePanelButtonCreator(PicturePanel pp, File imageFolder)
	{
		this.pp = pp;
		queue = new LinkedList<>();
		queueNumber = 0;
		
		for (var f: _settings.listFilesInOrder(imageFolder))
		{
			var name = f.getName();
			var suffix = name.substring(name.lastIndexOf('.') + 1);
			if (suffix.equalsIgnoreCase("PNG") || suffix.equalsIgnoreCase("JPG") || suffix.equalsIgnoreCase("JPEG"))
			{
				queue.add(f);
			}
		}
		buttons = new JButton[queue.size()];
	}
	
	public synchronized void run()
	{
		if (queue.isEmpty() == false)
		{
			var threads = new ButtonMakerThread[_settings.SYS_THREADS];
			
			for (var i = 0; i < threads.length; i++)
			{
				threads[i] = new ButtonMakerThread(ButtonMakerThread.class.getName() + "-" + i);
				threads[i].start();
			}
			
			for (var thread : threads)
			{
				try
				{
					thread.join();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			for (var button : buttons)
			{
				System.out.println(button.getText());
				pp.add(button);
			}
		}
	}
	
	private class ButtonMakerThread extends Thread
	{
		public ButtonMakerThread(String string)
		{
			super(string);
		}

		@Override
		public void run()
		{
			while (true)
			{
				File f;
				int w;
				synchronized (queue)
				{
					if (queue.isEmpty())
					{
						break;
					}
					f = queue.removeFirst();
					w = queueNumber++;
				}
				buttons[w] = pp.createPicturePanelButton(f);
			}
		}
	}
}
