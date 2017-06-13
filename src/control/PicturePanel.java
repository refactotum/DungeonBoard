package control;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import main.Settings;

/**
 * a scroll menu to display images on as buttons
 * @author McAJBen <McAJBen@gmail.com>
 * @since 1.0
 */
public abstract class PicturePanel extends JPanel {
	
	private static final long serialVersionUID = 2972394170217781329L;
	
	/**
	 * creates an instance of the {@code Picture Panel}
	 */
	public PicturePanel() {
		setLayout(new GridLayout(0, 4));
		setBorder(BorderFactory.createEmptyBorder());
	}
	
	/**
	 * adds a button to the panel by loading an image from file
	 * @param file the file of an image to add
	 * @param w the position of the button
	 */
	public void addButton(File file, int w) {
		try {
			JButton button = new JButton(
					file.getName(),
					new ImageIcon(ImageIO.read(file).getScaledInstance(100, 60, BufferedImage.SCALE_SMOOTH)));
			button.setMargin(new Insets(0, 0, 0, 0));
			button.setFocusPainted(false);
			button.setVerticalTextPosition(SwingConstants.BOTTOM);
			button.setHorizontalTextPosition(SwingConstants.CENTER);
			button.setBackground(Settings.DISABLE_COLOR);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JButton button = (JButton) arg0.getSource();
					String name = button.getText();
					if (button.getBackground() == Settings.DISABLE_COLOR) {
						select(name);
						button.setBackground(Settings.ENABLE_COLOR);
					}
					else {
						deselect(name);
						button.setBackground(Settings.DISABLE_COLOR);
					}
				}
			});
			while (true) {
				try {
					add(button, w);
					break;
				}
				catch (IllegalArgumentException e) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e2) {}
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Cannot load Image, file is probably too large\n" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * removes all images
	 */
	public void clearButtons() {
		for (Component c: getComponents()) {
			if (c.getClass().equals(JButton.class)) {
				remove(c);
			}
		}
	}
	
	/**
	 * called when an image is selected
	 * @param name the name of the image
	 */
	protected abstract void select(String name);
	
	/**
	 * called when an image is deselected
	 * @param name the name of the image
	 */
	protected abstract void deselect(String name);
}