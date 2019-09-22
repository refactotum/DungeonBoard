package common;

import java.awt.image.*;

public class ImageWrapper
{
	public BufferedImage systemImage;
	
	public ImageWrapper(BufferedImage systemImage)
	{
		this.systemImage = systemImage;
	}

	private Coords _size;
	public Coords size()
	{
		if (this._size == null)
		{
			this._size = new Coords
			(
				this.systemImage.getWidth(), this.systemImage.getHeight()
			);
		}
		return this._size;
	}
}
