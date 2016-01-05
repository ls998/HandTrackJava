package handtrack1;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.opencv.core.Mat;

public class ImagePanel extends JPanel implements IFootageOut {

	private BufferedImage image;

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

	@Override
	public void frameOut(Mat frame) {
		image = Util.mat2Img(frame);
	}
}
