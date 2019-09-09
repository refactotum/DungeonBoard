package main;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class Settings
{
	public static Settings Instance = new Settings();
	private Main _main = Main.Instance;

	public final String NAME = "Dungeon Board";
	
	public class Directories
	{
		public final File FOLDER = new File(new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator + NAME);
		public File PAINT_FOLDER;
		public final File[] FOLDERS =
		{
			new File(FOLDER + File.separator + "Layer"),
			new File(FOLDER + File.separator + "Image"),
			new File(FOLDER + File.separator + "Paint"),
			new File(FOLDER + File.separator + "Loading")
		};
		public final File DATA_FOLDER = new File(FOLDER + File.separator + "Data");

		public int PAINT_FOLDER_SIZE;
	}
	public Directories directories = new Directories();

	public class Icons
	{
		public final ImageIcon Program = load("icon.gif");
		public final ImageIcon Refresh = load("refresh.gif");
		public final ImageIcon Flip = load("flip.gif");
		public final ImageIcon Settings = load("settings.gif");
		public final ImageIcon Dvd = load("dvdlogo.gif");
		public final ImageIcon Dvd2 = load("dvdlogo2.gif");

		public final ImageIcon[] PenDirectionLocks =
		{
			load("squigle.gif"),
			load("vertical.gif"),
			load("horizontal.gif")
		};
		
		public final ImageIcon[] TouchpadDrawModes =
		{
			load("mouse.gif"),
			load("visible.gif"),
			load("invisible.gif"),
			load("move.gif")
		};
		
		public final ImageIcon[] PenShapes =
		{
			load("circle.gif"),
			load("square.gif"),
			load("rect.gif")
		};
	}
	public final Icons icons = new Icons();
	
	public final BufferedImage BLANK_CURSOR = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
	public BufferedImage PAINT_IMAGE;
	public BufferedImage PAINT_CONTROL_IMAGE;
	public final Dimension CONTROL_SIZE = new Dimension(900, 700);
	public Dimension DISPLAY_SIZE;

	public class Colors
	{
		public final Color ACTIVE = new Color(153, 255, 187);
		public final Color INACTIVE = new Color(255, 128, 128);
		public final Color ENABLE_COLOR = Color.GREEN;
		public final Color DISABLE_COLOR = Color.GRAY;
		public final Color CLEAR = new Color(100, 255, 100);
		public final Color OPAQUE = new Color(255, 100, 100);
		public final Color PINK = new Color(255, 0, 255);
		public final Color PINK_CLEAR = new Color(255, 0, 255, 25);
		public final Color BACKGROUND = new Color(153, 153, 153);
		public final Color CONTROL_BACKGROUND = new Color(200, 200, 200);
	}
	public final Colors colors = new Colors();

	public final boolean IS_WINDOWS = System.getProperty("os.name").startsWith("Windows");
	public boolean[] PAINT_IMAGES;
	public final int SYS_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
	public final int PAINT_GUIDE_SCALE = 3;
	public final int PAINT_PIXELS_PER_MASK_PIXEL = 5;

	public void load() throws SecurityException
	{
		for (var f: directories.FOLDERS)
		{
			if (f.exists() == false)
			{
				f.mkdirs();
			}
		}
		
		var dataFolder = directories.DATA_FOLDER;
		if (dataFolder.exists() == false)
		{
			dataFolder.mkdirs();
		}
		
		var imageThumbs = new File(dataFolder + File.separator + "Layer");
		imageThumbs.mkdirs();
		
		var layerThumbs = new File(dataFolder + File.separator + "Image");
		layerThumbs.mkdirs();
		
		var paintMasks = new File(dataFolder + File.separator + "Paint");
		paintMasks.mkdirs();
	}
	
	public ImageIcon load(String res)
	{
		return new ImageIcon(Settings.class.getResource("/resources/" + res));
	}
	
	public JButton createButton(String label)
	{
		var button = new JButton(label);
		button.setFocusPainted(false);
		button.setRolloverEnabled(false);
		return button;
	}
	
	public JButton createButton(ImageIcon imageIcon)
	{
		var button = new JButton(imageIcon);
		button.setFocusPainted(false);
		button.setRolloverEnabled(false);
		return button;
	}
	
	public File fileToThumb(File f)
	{
		return new File(directories.DATA_FOLDER.getAbsolutePath() + File.separator + f.getParentFile().getName() + File.separator + f.getName());
	}
	
	public File thumbToFile(File f)
	{
		return new File(directories.FOLDER.getAbsolutePath() + File.separator + f.getParentFile().getName() + File.separator + f.getName());
	}
	
	public File folderToDataFolder(File f)
	{
		return new File(directories.DATA_FOLDER.getAbsolutePath() + File.separator + f.getName());
	}
	
	public File fileToMaskFile(File f)
	{
		File returnValue = null;
		if (f != null)
		{
			var fileSuffix = ( f.isDirectory() ? ".f" : "");
			var filePath =
				directories.DATA_FOLDER.getAbsolutePath() + File.separator
				+ "Paint" + File.separator + f.getName() + fileSuffix;
			returnValue = new File(filePath);
		}
		return returnValue;
	}
	
	public void showError(String message)
	{
		JOptionPane.showMessageDialog(_main.getControl(), message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void showError(String message, Throwable error)
	{
		JOptionPane.showMessageDialog(_main.getControl(), message + "\n" + error.getMessage());
	}
	
	public LinkedList<File> listFilesInOrder(File folder)
	{
		LinkedList<File> files = new LinkedList<>();
		for (var f : folder.listFiles())
		{
			files.add(f);
		}
		files.sort(new Comparator<File>()
		{
			@Override
			public int compare(File o1, File o2)
			{
				return o1.compareTo(o2);
			}
		});
		return files;
	}
}
