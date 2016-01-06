package handtrack1;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JPanel;

import org.opencv.core.Mat;

public class ImagePanel extends JPanel implements IFootageOut {

	private BufferedImage image;

	public ImagePanel(int width, int height, int imageType) {
		image = new BufferedImage(width, height, imageType);
	}

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
		BufferedImage buffer = image;
		byte[] data = ((DataBufferByte) buffer.getRaster().getDataBuffer()).getData();
		frame.get(0, 0, data);
		this.repaint();
	}
}
