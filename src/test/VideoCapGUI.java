package test;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class VideoCapGUI {
	
	static{

	      System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	}

	static class ImagePanel extends JPanel {

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

	private JFrame frame;
	private VideoCapture capture;
	private ScheduledExecutorService timer;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VideoCapGUI window = new VideoCapGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public VideoCapGUI() {
		initialize();
		initCam();
	}

	private Mat cap = new Mat();
	private ImagePanel panel;

	private void initCam() {
		capture = new VideoCapture();
		capture.open(0);
		if (capture.isOpened()) {
			Runnable frameGrabber = new Runnable() {

				@Override
				public void run() {
					if (capture.isOpened()) {
						capture.read(cap);
						Imgproc.cvtColor(cap, cap, Imgproc.COLOR_BGR2GRAY);
						BufferedImage gray;
						if (panel.image == null) {
							gray = new BufferedImage(cap.width(), cap.height(), BufferedImage.TYPE_BYTE_GRAY);
							panel.image = gray;
						}
						gray = panel.image;
						byte[] data = ((DataBufferByte) gray.getRaster().getDataBuffer()).getData();
						cap.get(0, 0, data);
						frame.repaint();
					}else{
						System.exit(1);
					}
				}
			};
			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new ImagePanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
	}

}
