package paint;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

import main.*;

public class DrawPanel extends JComponent
{
	private int penRadiusDefault = 25;
	private int penRadius;
	private int penDiameter;
	private PenShape penShape;
	private BufferedImage drawingLayer;
	private Graphics2D g2;
	private Dimension controlSize;
	private double displayZoom;
	private Point lastMouseClickPosition;
	private Point mousePos;
	private boolean canDraw;
	private boolean isLoading;
	private boolean isDragging;
	private PenDirection penDirectionLock;
	private TouchpadDrawMode touchpadDrawMode;
	private Point lastWindowClick;
	private Point windowPosition;
	private Point startOfClick;

	private JButton updateButton;

	private ControlBuilder _controlBuilder = ControlBuilder.Instance;
	private FileHelper _fileHelper = FileHelper.Instance;
	private PaintHelper _paintHelper = PaintHelper.Instance;
	
	private Main _main = Main.Instance;

	public DrawPanel()
	{
		setDefaults();
		createUpdateButton();
		createAndAddMouseListener();
		createAndAddMouseMotionListener();
		createAndAddComponentListener();
		repaint();
	}
	
	private void setDefaults()
	{
		setDoubleBuffered(false);
		setPenRadius(penRadiusDefault);
		mousePos = new Point(-100, -100);
		displayZoom = 1;
		windowPosition = new Point(0, 0);
		lastWindowClick = new Point(0, 0);
		penShape = PenShape.Circle;
		penDirectionLock = PenDirection.None;
		touchpadDrawMode = TouchpadDrawMode.Any;
	}
	
	private void createUpdateButton()
	{
		var colors = _controlBuilder.colors;

		updateButton = _controlBuilder.createButton
		(
			"Update Screen",
			e ->
			{
				if (hasImage())
				{
					try
					{
						_main.displayPaint.setMask(getMask());
					}
					catch (OutOfMemoryError error)
					{
						_controlBuilder.showError(this, "Cannot update Image, file is probably large", error);
					}
					updateButton.setEnabled(false);
					updateButton.setBackground(colors.controlBackground);
				}
			}
		);
	}
	
	private void createAndAddMouseListener()
	{
		var colors = _controlBuilder.colors;

		var mouseAdapter = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (_paintHelper.paintImage != null)
				{
					lastMouseClickPosition = toDrawingPoint(e.getPoint());
					switch (touchpadDrawMode)
					{
					case Any:
						if (e.getButton() == MouseEvent.BUTTON2)
						{
							setWindowPosition(lastMouseClickPosition);
							_main.displayPaint.setWindowPosition(getWindowPosition());
							canDraw = false;
						}
						else
						{
							if (e.getButton() == MouseEvent.BUTTON1)
							{
								g2.setPaint(colors.clear);
								canDraw = true;
							}
							else if (e.getButton() == MouseEvent.BUTTON3)
							{
								g2.setPaint(colors.opaque);
								canDraw = true;
							}
							startOfClick = e.getPoint();
							isDragging = true;
							addPoint(lastMouseClickPosition);
						}
						break;
					case Invisible:
					case Visible:
						startOfClick = e.getPoint();
						isDragging = true;
						addPoint(lastMouseClickPosition);
						break;
					case Window:
						setWindowPosition(lastMouseClickPosition);
						_main.displayPaint.setWindowPosition(getWindowPosition());
						break;
					}
					repaint();
				}
			}
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (_paintHelper.paintImage != null && canDraw)
				{
					switch (penShape){
					case Rectangle:
						var p = toDrawingPoint(e.getPoint());
						var p2 = toDrawingPoint(startOfClick);
						g2.fillRect(
							Math.min(p.x, p2.x),
							Math.min(p.y, p2.y),
							Math.abs(p.x - p2.x),
							Math.abs(p.y - p2.y)
						);
						break;
					default:
						break;
					}
				}
				isDragging = false;
				repaint();
			}
		};
		addMouseListener(mouseAdapter);
	}

	private void createAndAddMouseMotionListener()
	{
		var mouseMotionAdapter = new MouseMotionAdapter()
		{
			public void mouseDragged(MouseEvent e)
			{
				if (_paintHelper.paintImage != null)
				{
					if (canDraw)
					{
						addPoint(toDrawingPoint(e.getPoint()));
					}
					else
					{
						setWindowPosition(toDrawingPoint(e.getPoint()));
						_main.displayPaint.setWindowPosition(getWindowPosition());
					}
					mousePos = e.getPoint();
					repaint();
				}
			}
			public void mouseMoved(MouseEvent e)
			{
				mousePos = e.getPoint();
				repaint();
			}
		};
		addMouseMotionListener(mouseMotionAdapter);
	}

	private void createAndAddComponentListener()
	{
		var componentListener = new ComponentListener()
		{
			public void componentShown(ComponentEvent e) {}
			public void componentResized(ComponentEvent e)
			{
				controlSize = getSize();
				repaint();
			}
			public void componentMoved(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
		};
		addComponentListener(componentListener);
	}

	public void setZoom(double zoom)
	{
		// A higher number will zoom out.
		displayZoom = zoom;
		setWindowPosition(lastWindowClick);
		_main.displayPaint.setWindowScaleAndPosition(zoom, getWindowPosition());
		repaint();
	}

	public void setWindowScaleAndPosition(double zoom, Point p)
	{
		displayZoom = zoom;
		setWindowPosition(p);
		_main.displayPaint.setWindowScaleAndPosition(zoom, getWindowPosition());
		repaint();
	}

	public synchronized void setImage()
	{
		if (_paintHelper.paintImage != null)
		{
			var paintFolder = _fileHelper.paintFolder;
			var maskFile = _fileHelper.fileToMaskFile(paintFolder);

			if (maskFile.exists() && maskFile.lastModified() > paintFolder.lastModified())
			{
				try
				{
					drawingLayer = ImageIO.read(maskFile);
					g2 = (Graphics2D) drawingLayer.getGraphics();
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.6f));
					g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
				}
				catch (IOException e)
				{
					_controlBuilder.showError(this, "Cannot load Mask, file is probably too large", e);
				}
			}
			else
			{
				var paintImage = _paintHelper.paintImage;
				var pixelsPerMask = _paintHelper.paintPixelsPerMaskPixel;
				drawingLayer = new BufferedImage
				(
					paintImage.getWidth() / pixelsPerMask,
					paintImage.getHeight() / pixelsPerMask,
					BufferedImage.TYPE_INT_ARGB
				);
				g2 = (Graphics2D) drawingLayer.getGraphics();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.6f));
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
				hideAll();
			}
		}
	}

	public void setPenRadius(int value)
	{
		penRadius = value;
		penDiameter = penRadius * 2;
		repaint();
	}

	public JButton getUpdateButton()
	{
		return updateButton;
	}

	public void resetImage()
	{
		_paintHelper.paintImage = null;
		_paintHelper.paintControlImage = null;
		g2 = null;
		drawingLayer = null;
		isLoading = false;
	}

	public void togglePenShape()
	{
		var penShapes = PenShape.values();
		penShape = penShapes[(penShape.ordinal() + 1) % penShapes.length];
		repaint();
	}

	public void togglePenDirectionLock()
	{
		var penDirections = PenDirection.values();
		penDirectionLock = penDirections[(penDirectionLock.ordinal() + 1) % penDirections.length];
	}

	public void toggleTouchpadDrawMode()
	{
		var touchpadDrawModes = TouchpadDrawMode.values();
		touchpadDrawMode =
			touchpadDrawModes[(touchpadDrawMode.ordinal() + 1) % touchpadDrawModes.length];

		var colors = _controlBuilder.colors;

		if (g2 != null)
		{
			switch (touchpadDrawMode) {
			case Any:
				break;
			case Visible:
				g2.setPaint(colors.clear);
				canDraw = true;
				break;
			case Invisible:
				g2.setPaint(colors.opaque);
				canDraw = true;
				break;
			case Window:
				canDraw = false;
				break;
			}
		}
	}

	public void setIsImageLoading(boolean value)
	{
		isLoading = value;
		repaint();
	}

	public int getPenShape()
	{
		return penShape.ordinal();
	}

	public int getStyle()
	{
		return penDirectionLock.ordinal();
	}

	public int getTouchpadDrawMode()
	{
		return touchpadDrawMode.ordinal();
	}

	public BufferedImage getMask() throws OutOfMemoryError
	{
		var mask = new BufferedImage
		(
			drawingLayer.getWidth(),
			drawingLayer.getHeight(),
			BufferedImage.TYPE_INT_ARGB
		);

		var colorClearAsRgb = -1721434268;
		var colorOpaqueAsRgb = -1711315868;

		for (var i = 0; i < drawingLayer.getWidth(); i++)
		{
			for (var j = 0; j < drawingLayer.getHeight(); j++)
			{
				var dl = drawingLayer.getRGB(i, j);
				if (dl == colorClearAsRgb)
				{
					mask.setRGB(i, j, 0);
				}
				else if (dl == colorOpaqueAsRgb)
				{
					mask.setRGB(i, j, -16777215);
				}
			}
		}
		return mask;
	}

	public Point getWindowPosition()
	{
		return new Point((int)(windowPosition.x / displayZoom), (int) (windowPosition.y / displayZoom));
	}

	public boolean hasImage()
	{
		return drawingLayer != null;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		var colors = _controlBuilder.colors;

		var g2d = (Graphics2D) g;
		if (isLoading)
		{
			g2d.drawString("Loading...", controlSize.width / 2, controlSize.height / 2);
		}
		else if (_paintHelper.paintControlImage != null)
		{
			g2d.drawImage(_paintHelper.paintControlImage, 0, 0, controlSize.width, controlSize.height, null);
			g2d.drawImage(drawingLayer, 0, 0, controlSize.width, controlSize.height, null);
			g2d.setColor(colors.pink);
			switch (penShape) {
			case Circle:
				g2d.drawOval(mousePos.x - penRadius, mousePos.y - penRadius, penDiameter, penDiameter);
				break;
			case Square:
				g2d.drawRect(mousePos.x - penRadius, mousePos.y - penRadius, penDiameter, penDiameter);
				break;
			case Rectangle:
				if (isDragging)
				{
					g2d.drawRect
					(
						Math.min(mousePos.x, startOfClick.x),
						Math.min(mousePos.y, startOfClick.y),
						Math.abs(mousePos.x - startOfClick.x),
						Math.abs(mousePos.y - startOfClick.y)
					);
				}
				g2d.drawLine(mousePos.x, mousePos.y - 10, mousePos.x, mousePos.y + 10);
				g2d.drawLine(mousePos.x - 10, mousePos.y, mousePos.x + 10, mousePos.y);
				break;
			}

			drawPlayerView(g2d);
		}
		else if (controlSize != null)
		{
			g2d.drawString("No image loaded", controlSize.width / 2, controlSize.height / 2);
		}
	}

	private void drawPlayerView(Graphics2D g2d)
	{
		var displaySize = _paintHelper.displaySize;
		var paintImage = _paintHelper.paintImage;
		var w = (int) (displaySize.width * displayZoom * controlSize.width / paintImage.getWidth());
		var h = (int) (displaySize.height * displayZoom * controlSize.height / paintImage.getHeight());
		int x, y;

		if (w > controlSize.width)
		{
			x = -(w - controlSize.width) / 2;
		}
		else
		{
			x = windowPosition.x * controlSize.width / paintImage.getWidth();
		}
		if (h > controlSize.height)
		{
			y = -(h - controlSize.height) / 2;
		}
		else
		{
			y = windowPosition.y * controlSize.height / paintImage.getHeight();
		}

		g2d.drawRect(x, y, w, h);
		g2d.drawLine(x, y, x + w, y + h);
		g2d.drawLine(x + w, y, x, y + h);
		g2d.setColor(_controlBuilder.colors.pinkClear);
		g2d.fillRect(x, y, w, h);
	}

	private Point toDrawingPoint(Point p)
	{
		return new Point
		(
			p.x * drawingLayer.getWidth() / controlSize.width,
			p.y * drawingLayer.getHeight() / controlSize.height
		);
	}

	private void setWindowPosition(Point p)
	{
		lastWindowClick = p;

		var pixelsPerMask = _paintHelper.paintPixelsPerMaskPixel;
		var displaySize = _paintHelper.displaySize;
		windowPosition.x = (int) (p.x * pixelsPerMask - (displaySize.width * displayZoom) / 2);
		windowPosition.y = (int) (p.y * pixelsPerMask - (displaySize.height * displayZoom) / 2);

		var paintImage = _paintHelper.paintImage;
		if (paintImage != null)
		{
			var xMax = paintImage.getWidth() - displaySize.width * displayZoom;
			if (windowPosition.x > xMax)
			{
				windowPosition.x = (int)xMax;
			}
			if (windowPosition.x < 0)
			{
				windowPosition.x = 0;
			}
			var yMax = paintImage.getHeight() - displaySize.height * displayZoom;
			if (windowPosition.y > yMax)
			{
				windowPosition.y = (int)yMax;
			}
			if (windowPosition.y < 0)
			{
				windowPosition.y = 0;
			}
		}
	}

	/**
	 * uses the pen to draw onto the {@code drawingLayer}
	 * @param newP a point based on the placement on {@code _settings.paintImage}<br>
	 * use {@code toDrawingPoint} to convert to the correct point
	 */
	private void addPoint(Point newP)
	{
		if (g2 != null)
		{
			var colors = _controlBuilder.colors;

			switch (penDirectionLock)
			{
				case Horizontal:
					newP.y = lastMouseClickPosition.y;
					break;
				case Vertical:
					newP.x = lastMouseClickPosition.x;
					break;
				default:
					break;
			}
			final double widthMod = (double)drawingLayer.getWidth() / controlSize.width;
			final double heightMod = (double)drawingLayer.getHeight() / controlSize.height;
			final double rwidth = penRadius * widthMod;
			final double rheight = penRadius * heightMod;
			final int dwidth = (int) (penDiameter * widthMod);
			final int dheight = (int) (penDiameter * heightMod);
			switch (penShape) {
			case Circle:
				g2.fillPolygon(getPolygonSweptByOval(newP, lastMouseClickPosition, rwidth, rheight));
				g2.fillOval
				(
					newP.x - (int)rwidth,
					newP.y - (int)rheight,
					dwidth,
					dheight
				);
				break;
			case Square:
				g2.fillPolygon(getPolygonSweptByRectangle(newP, lastMouseClickPosition, (int)rwidth, (int)rheight));
				g2.fillRect
				(
					newP.x - (int)rwidth,
					newP.y - (int)rheight,
					dwidth,
					dheight
				);
				break;
			case Rectangle:
				break;
			}
			lastMouseClickPosition = newP;
			updateButton.setEnabled(true);
			updateButton.setBackground(colors.active);
		}
	}

	private Polygon getPolygonSweptByOval(Point newP, Point oldP, double rwidth, double rheight)
	{
		final double angle = -Math.atan2(newP.getY() - oldP.getY(), newP.getX() - oldP.getX());
		final double anglePos = angle + Math.PI / 2;
		final double angleNeg = angle - Math.PI / 2;
		final int cosP = (int) (Math.cos(anglePos) * rwidth);
		final int cosN = (int) (Math.cos(angleNeg) * rwidth);
		final int sinP = (int) (Math.sin(anglePos) * rheight);
		final int sinN = (int) (Math.sin(angleNeg) * rheight);
		return new Polygon
		(
			new int[] {
					newP.x + cosP,
					newP.x + cosN,
					oldP.x + cosN,
					oldP.x + cosP},
			new int[] {
					newP.y - sinP,
					newP.y - sinN,
					oldP.y - sinN,
					oldP.y - sinP},
			4
		);
	}

	private Polygon getPolygonSweptByRectangle(Point newP, Point oldP, int rwidth, int rheight)
	{
		if ((newP.x > oldP.x && newP.y > oldP.y) || (newP.x < oldP.x && newP.y < oldP.y))
		{
			rheight *= -1;
		}
		return new Polygon
		(
			new int[]
			{
				newP.x - rwidth,
				newP.x + rwidth,
				oldP.x + rwidth,
				oldP.x - rwidth
			},
			new int[]
			{
				newP.y - rheight,
				newP.y + rheight,
				oldP.y + rheight,
				oldP.y - rheight
			},
			4 
		);
	}

	private void fillAll(Color c)
	{
		if (g2 != null)
		{
			g2.setPaint(c);
			g2.fillRect(0, 0, drawingLayer.getWidth(), drawingLayer.getHeight());
			repaint();
			updateButton.setEnabled(true);
			updateButton.setBackground(_controlBuilder.colors.active);
		}
	}

	public void hideAll()
	{
		fillAll(_controlBuilder.colors.opaque);
	}

	public void showAll()
	{
		fillAll(_controlBuilder.colors.clear);
	}

	public void saveMask()
	{
		var fileHelper = _fileHelper;
		var f = fileHelper.fileToMaskFile(fileHelper.paintFolder);
		if (f != null)
		{
			try
			{
				ImageIO.write(drawingLayer, "png", f);

				var dataFilePath = 
					fileHelper.dataFolder + File.separator
					+ "Paint" + File.separator + f.getName() + ".data";
				var dataFile = _fileHelper.getFileAtPath(dataFilePath);
				var writer = new BufferedWriter(new FileWriter(dataFile));
				writer.write(String.format("%f %d %d", displayZoom, lastWindowClick.x, lastWindowClick.y));
				writer.close();

			}
			catch (IOException e)
			{
				_controlBuilder.showError(this, "Cannot save Mask", e);
			}
		}
	}
}
