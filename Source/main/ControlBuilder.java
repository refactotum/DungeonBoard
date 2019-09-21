package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class ControlBuilder
{
	public static ControlBuilder Instance = new ControlBuilder();

	public class Colors
	{
		public final Color active = new Color(153, 255, 187);
		public final Color inactive = new Color(255, 128, 128);
		public final Color enableColor = Color.GREEN;
		public final Color disableColor = Color.GRAY;
		public final Color clear = new Color(100, 255, 100);
		public final Color opaque = new Color(255, 100, 100);
		public final Color pink = new Color(255, 0, 255);
		public final Color pinkClear = new Color(255, 0, 255, 25);
		public final Color background = new Color(153, 153, 153);
		public final Color controlBackground = new Color(200, 200, 200);
	}
	public final Colors colors = new Colors();

	public final Dimension controlSize = new Dimension(900, 700);

	public JButton createButton(String label)
	{
		return this.createButton(label, null, null);
	}

	public JButton createButton(String label, Color backgroundColor)
	{
		return this.createButton(label, backgroundColor, null);
	}

	public JButton createButton(String label, ActionListener actionListener)
	{
		return this.createButton(label, null, actionListener);
	}

	public JButton createButton
	(
		String label, Color backgroundColor, ActionListener actionListener
	)
	{
		var button = new JButton(label);
		button.setFocusPainted(false);
		button.setRolloverEnabled(false);
		if (backgroundColor != null)
		{
			button.setBackground(backgroundColor);
		}
		if (actionListener != null)
		{
			button.addActionListener(actionListener);
		}
		return button;
	}

	public JButton createButton(ImageIcon imageIcon)
	{
		return this.createButton(imageIcon, null, null);
	}

	public JButton createButton(ImageIcon imageIcon, ActionListener actionListener)
	{
		return this.createButton(imageIcon, null, actionListener);
	}

	public JButton createButton(ImageIcon imageIcon, Color backgroundColor, ActionListener actionListener)
	{
		var button = new JButton(imageIcon);
		button.setFocusPainted(false);
		button.setRolloverEnabled(false);
		if (backgroundColor != null)
		{
			button.setBackground(backgroundColor);
		}
		if (actionListener != null)
		{
			button.addActionListener(actionListener);
		}
		return button;
	}

	public <T> JComboBox<T> createComboBox(T[] items)
	{
		return this.createComboBox(items, null);
	}

	public <T> JComboBox<T> createComboBox(T[] items, Color backgroundColor)
	{
		var returnValue = new JComboBox<T>(items);
		if (backgroundColor != null)
		{
			returnValue.setBackground(backgroundColor);
		}
		return returnValue;
	}

	public JLabel createLabel(String text, int horizontalAlignment)
	{
		return this.createLabel(text, horizontalAlignment, null);
	}

	public JLabel createLabel(String text, int horizontalAlignment, Color backgroundColor)
	{
		var returnValue = new JLabel(text);
		if (backgroundColor != null)
		{
			returnValue.setBackground(backgroundColor);
		}
		if (horizontalAlignment >= 0)
		{
			returnValue.setHorizontalAlignment(horizontalAlignment);
		}
		return returnValue;
	}

	public JPanel createPanel(LayoutManager layout)
	{
		return this.createPanel(layout, null);
	}

	public JPanel createPanel(LayoutManager layout, Color backgroundColor)
	{
		var returnValue = new JPanel(layout);
		if (backgroundColor != null)
		{
			returnValue.setBackground(backgroundColor);
		}
		return returnValue;
	}

	public JPanel createPanelWithBoxLayout(int axis)
	{
		return this.createPanelWithBoxLayout(axis, null);
	}

	public JPanel createPanelWithBoxLayout(int axis, Color backgroundColor)
	{
		var returnValue = new JPanel();
		returnValue.setLayout(new BoxLayout(returnValue, axis));
		if (backgroundColor != null)
		{
			returnValue.setBackground(backgroundColor);
		}
		return returnValue;
	}

	public JScrollPane createScrollPane(JComponent component)
	{
		return new JScrollPane(component);
	}

	public JSlider createSlider
	(
		int orientation, int min, int max, int value, 
		Dimension minSize, Color backgroundColor, ChangeListener changeListener
	)
	{
		var returnValue = new JSlider(orientation, min, max, value);
		if (minSize != null)
		{
			returnValue.setMinimumSize(minSize);
		}
		if (backgroundColor != null)
		{
			returnValue.setBackground(backgroundColor);
		}
		if (changeListener != null)
		{
			returnValue.addChangeListener(changeListener);
		}
		return returnValue;
	}

	public JTextField createTextField(String text, int columns)
	{
		var returnValue = new JTextField(text, columns);
		return returnValue;
	}
}
