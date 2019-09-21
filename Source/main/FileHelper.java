package main;

import java.io.*;
import java.util.*;

public class FileHelper
{
	public static FileHelper Instance = new FileHelper();

	public File folder;
	public File[] folders;
	public File dataFolder;
	public File paintFolder;
	public int paintFolderSize;
	
	public FileHelper()
	{
		var applicationName = Main.ApplicationName;

		var fileSeparator = File.separator;

		this.folder = new File
		(
			new File(System.getProperty("user.dir")).getAbsolutePath()
			+ fileSeparator + applicationName
		);

		this.folders = new File[]
		{
			new File(folder + fileSeparator + "Layer"),
			new File(folder + fileSeparator + "Image"),
			new File(folder + fileSeparator + "Paint"),
			new File(folder + fileSeparator + "Loading")
		};

		this.dataFolder = new File(folder + fileSeparator + "Data");
	}
	
	public boolean isFileAnImage(File f)
	{
		var name = f.getName();
		var suffix = name.substring(name.lastIndexOf('.') + 1);
		var returnValue = 
		(
			suffix.equalsIgnoreCase("PNG")
			|| suffix.equalsIgnoreCase("JPG")
			|| suffix.equalsIgnoreCase("JPEG")
		);
		return returnValue;
	}

	public void load() throws SecurityException
	{
		for (var f : this.folders)
		{
			if (f.exists() == false)
			{
				f.mkdirs();
			}
		}

		var dataFolder = this.dataFolder;
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
