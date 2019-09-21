package main;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class Settings
{
	public static Settings Instance = new Settings();
	private Main _main = Main.Instance;

	public final String applicationName = "Dungeon Board";

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

		public ImageIcon load(String resourceName)
		{
			return new ImageIcon(Settings.class.getResource("/resources/" + resourceName));
		}
	}
	public final Icons icons = new Icons();

	public class PaintHelper
	{
		public final BufferedImage blankCursor = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
		public BufferedImage paintImage;
		public BufferedImage paintControlImage;
		public Dimension displaySize;

		public final boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		public boolean[] paintImageS; // "S"?
		public final int sysThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		public final int paintGuideScale = 3;
		public final int paintPixelsPerMaskPixel = 5;
	}
	public final PaintHelper paintHelper = new PaintHelper();

	public class FileHelper
	{
		private final String fileSeparator = File.separator;
		public final File folder = new File
		(
			new File(System.getProperty("user.dir")).getAbsolutePath()
			+ fileSeparator + applicationName
		);
		public File paintFolder;
		public final File[] folders =
		{
			new File(folder + fileSeparator + "Layer"),
			new File(folder + fileSeparator + "Image"),
			new File(folder + fileSeparator + "Paint"),
			new File(folder + fileSeparator + "Loading")
		};
		public final File dataFolder = new File(folder + fileSeparator + "Data");

		public int paintFolderSize;

		public void load() throws SecurityException
		{
			for (var f : fileHelper.folders)
			{
				if (f.exists() == false)
				{
					f.mkdirs();
				}
			}

			var dataFolder = fileHelper.dataFolder;
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

		public File fileToThumb(File f)
		{
			return new File(this.dataFolder.getAbsolutePath() + File.separator + f.getParentFile().getName() + File.separator + f.getName());
		}

		public File thumbToFile(File f)
		{
			return new File(this.folder.getAbsolutePath() + File.separator + f.getParentFile().getName() + File.separator + f.getName());
		}

		public File folderToDataFolder(File f)
		{
			return new File(this.dataFolder.getAbsolutePath() + File.separator + f.getName());
		}

		public File fileToMaskFile(File f)
		{
			File returnValue = null;
			if (f != null)
			{
				var fileSuffix = ( f.isDirectory() ? ".f" : "");
				var filePath =
					this.dataFolder.getAbsolutePath() + File.separator
					+ "Paint" + File.separator + f.getName() + fileSuffix;
				returnValue = new File(filePath);
			}
			return returnValue;
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
	public final FileHelper fileHelper = new FileHelper();
}
