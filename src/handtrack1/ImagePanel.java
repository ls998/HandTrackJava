package handtrack1;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

	public BufferedImage image;

	/**
	 * 
	 */
	private static final long serialVersionUID = 2763509481815891725L;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null)
			g.drawImage(image, 0, 0, null); // see javadoc for more info on
											// the
											// parameters
	}
}
