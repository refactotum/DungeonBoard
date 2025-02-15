package paint;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

import common.*;
import main.*;

public class DrawPanel extends JComponent
{
	private int penRadiusDefault = 25;
	private int penRadius;
	private int penDiameter;
	private PenShape penShape;
	private ImageWrapper drawingLayer;
	private Graphics2D g2;
	private Coords controlSize;
	private double displayZoom;
	private Coords lastMouseClickPosition;
	private Coords mousePos;
	private boolean canDraw;
	private boolean isLoading;
	private boolean isDragging;
	private PenDirection penDirectionLock;
	private TouchpadDrawMode touchpadDrawMode;
	private Coords lastWindowClick;
	private Coords windowPosition;
	private Coords startOfClick;

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
		mousePos = new Coords(-100, -100);
		displayZoom = 1;
		windowPosition = new Coords(0, 0);
		lastWindowClick = new Coords(0, 0);
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
					lastMouseClickPosition = toDrawingPoint(Coords.fromPoint(e.getPoint()));
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
							startOfClick = Coords.fromPoint(e.getPoint());
							isDragging = true;
							addPoint(lastMouseClickPosition);
						}
						break;
					case Invisible:
					case Visible:
						startOfClick = Coords.fromPoint(e.getPoint());
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
						var p = toDrawingPoint(Coords.fromPoint(e.getPoint()));
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
						addPoint(toDrawingPoint(Coords.fromPoint(e.getPoint())));
					}
					else
					{
						setWindowPosition(toDrawingPoint(Coords.fromPoint(e.getPoint())));
						_main.displayPaint.setWindowPosition(getWindowPosition());
					}
					mousePos = Coords.fromPoint(e.getPoint());
					repaint();
				}
			}
			public void mouseMoved(MouseEvent e)
			{
				mousePos = Coords.fromPoint(e.getPoint());
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
				controlSize = Coords.fromDimension(getSize());
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

	public void setWindowScaleAndPosition(double zoom, Coords p)
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
					drawingLayer = new ImageWrapper(ImageIO.read(maskFile));
					g2 = (Graphics2D) drawingLayer.systemImage.getGraphics();
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
				var paintImageSize = _paintHelper.paintImage.size();
				var pixelsPerMask = _paintHelper.paintPixelsPerMaskPixel;
				drawingLayer = new ImageWrapper
				(
					new BufferedImage
					(
						paintImageSize.x / pixelsPerMask,
						paintImageSize.y / pixelsPerMask,
						BufferedImage.TYPE_INT_ARGB
					)
				);
				g2 = (Graphics2D) drawingLayer.systemImage.getGraphics();
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

	public ImageWrapper getMask() throws OutOfMemoryError
	{
		var drawingLayerSize = drawingLayer.size();

		var mask = new BufferedImage
		(
			drawingLayerSize.x,
			drawingLayerSize.y,
			BufferedImage.TYPE_INT_ARGB
		);

		var colorClearAsRgb = -1721434268;
		var colorOpaqueAsRgb = -1711315868;

		for (var i = 0; i < drawingLayerSize.x; i++)
		{
			for (var j = 0; j < drawingLayerSize.y; j++)
			{
				var dl = drawingLayer.systemImage.getRGB(i, j);
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

		return new ImageWrapper(mask);
	}

	public Coords getWindowPosition()
	{
		return new Coords((int)(windowPosition.x / displayZoom), (int) (windowPosition.y / displayZoom));
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
			g2d.drawString("Loading...", controlSize.x / 2, controlSize.y / 2);
		}
		else if (_paintHelper.paintControlImage != null)
		{
			g2d.drawImage(_paintHelper.paintControlImage.systemImage, 0, 0, controlSize.x, controlSize.y, null);
			g2d.drawImage(drawingLayer.systemImage, 0, 0, controlSize.x, controlSize.y, null);
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
			g2d.drawString("No image loaded", controlSize.x / 2, controlSize.y / 2);
		}
	}

	private void drawPlayerView(Graphics2D g2d)
	{
		var displaySize = _paintHelper.displaySize;
		var paintImageSize = _paintHelper.paintImage.size();
		var w = (int) (displaySize.x * displayZoom * controlSize.x / paintImageSize.x);
		var h = (int) (displaySize.y * displayZoom * controlSize.y / paintImageSize.y);
		int x, y;

		if (w > controlSize.x)
		{
			x = -(w - controlSize.x) / 2;
		}
		else
		{
			x = windowPosition.x * controlSize.x / paintImageSize.x;
		}
		if (h > controlSize.y)
		{
			y = -(h - controlSize.y) / 2;
		}
		else
		{
			y = windowPosition.y * controlSize.y / paintImageSize.y;
		}

		g2d.drawRect(x, y, w, h);
		g2d.drawLine(x, y, x + w, y + h);
		g2d.drawLine(x + w, y, x, y + h);
		g2d.setColor(_controlBuilder.colors.pinkClear);
		g2d.fillRect(x, y, w, h);
	}

	private Coords toDrawingPoint(Coords p)
	{
		var drawingLayerSize = drawingLayer.size();
		return new Coords
		(
			p.x * drawingLayerSize.x / controlSize.x,
			p.y * drawingLayerSize.y / controlSize.y
		);
	}

	private void setWindowPosition(Coords p)
	{
		lastWindowClick = p;

		var pixelsPerMask = _paintHelper.paintPixelsPerMaskPixel;
		var displaySize = _paintHelper.displaySize;
		windowPosition.x = (int) (p.x * pixelsPerMask - (displaySize.x * displayZoom) / 2);
		windowPosition.y = (int) (p.y * pixelsPerMask - (displaySize.y * displayZoom) / 2);

		var paintImage = _paintHelper.paintImage;
		if (paintImage != null)
		{
			var paintImageSize = paintImage.size();
			var xMax = paintImageSize.x - displaySize.x * displayZoom;
			if (windowPosition.x > xMax)
			{
				windowPosition.x = (int)xMax;
			}
			if (windowPosition.x < 0)
			{
				windowPosition.x = 0;
			}
			var yMax = paintImageSize.y - displaySize.y * displayZoom;
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
	private void addPoint(Coords newP)
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
			var drawingLayerSize = drawingLayer.size();
			final double widthMod = (double)drawingLayerSize.x / controlSize.x;
			final double heightMod = (double)drawingLayerSize.y / controlSize.y;
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

	private Polygon getPolygonSweptByOval(Coords newP, Coords oldP, double rwidth, double rheight)
	{
		final double angle = -Math.atan2(newP.y - oldP.y, newP.x - oldP.x);
		final double anglePos = angle + Math.PI / 2;
		final double angleNeg = angle - Math.PI / 2;
		final int cosP = (int) (Math.cos(anglePos) * rwidth);
		final int cosN = (int) (Math.cos(angleNeg) * rwidth);
		final int sinP = (int) (Math.sin(anglePos) * rheight);
		final int sinN = (int) (Math.sin(angleNeg) * rheight);
		return new Polygon
		(
			new int[]
			{
				newP.x + cosP,
				newP.x + cosN,
				oldP.x + cosN,
				oldP.x + cosP
			},
			new int[]
			{
				newP.y - sinP,
				newP.y - sinN,
				oldP.y - sinN,
				oldP.y - sinP
			},
			4
		);
	}

	private Polygon getPolygonSweptByRectangle(Coords newP, Coords oldP, int rwidth, int rheight)
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
			var drawingLayerSize = drawingLayer.size();
			g2.fillRect(0, 0, drawingLayerSize.x, drawingLayerSize.y);
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
				ImageIO.write(drawingLayer.systemImage, "png", f);

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
