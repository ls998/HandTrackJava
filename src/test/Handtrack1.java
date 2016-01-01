package test;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class Handtrack1 {

	static {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
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
					Handtrack1 window = new Handtrack1();
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
	public Handtrack1() {
		initialize();
		initCam();
	}

	private Mat kernel = new Mat();

	private Scalar lower = new Scalar(0, 0, 0);
	private Scalar upper = new Scalar(20, 255, 255);
	private Scalar range = new Scalar(20, 50, 50);
	private Scalar mean = new Scalar(0, 0, 0);

	private void hand(Mat im) {
		// resize to for faster speed
		// Size sz = new Size(im.width() / 2, im.height() / 2);

		// Imgproc.resize(im, im, sz);

		// convert to hsv colour space
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2HSV);

		// filter skin
		Mat binImg = new Mat();
		Core.inRange(im, lower, upper, binImg);

		// DESTROY SPECKSSSQ!!!!
		Imgproc.morphologyEx(binImg, binImg, Imgproc.MORPH_OPEN, kernel);
		// Imgproc.morphologyEx(binImg, binImg, Imgproc.MORPH_OPEN, kernel);
		// Imgproc.morphologyEx(binImg, binImg, Imgproc.MORPH_CLOSE, kernel);

		MatOfPoint bigContour = bigCont(binImg);

		Imgproc.cvtColor(binImg, im, Imgproc.COLOR_GRAY2BGR);

		if (bigContour != null) {
			ArrayList<MatOfPoint> derp = new ArrayList<>();
			derp.add(bigContour);
			Imgproc.drawContours(im, derp, 0, new Scalar(255, 0, 255));
		}
	}

	private void calibrate(Mat im) {
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2HSV);
		Mat rect = new Mat(im, new Rect(new Point(100, 100), new Point(150, 150)));
		mean = Core.mean(rect);
		int i;
		i = 0;
		lower.val[i] = Math.max(0, mean.val[i] - range.val[i]);
		i = 1;
		lower.val[i] = Math.max(0, mean.val[i] - range.val[i]);
		i = 2;
		lower.val[i] = Math.max(0, mean.val[i] - range.val[i]);
		i = 0;
		upper.val[i] = Math.min(179, mean.val[i] + range.val[i]);
		i = 1;
		upper.val[i] = Math.min(255, mean.val[i] + range.val[i]);
		i = 2;
		upper.val[i] = Math.min(255, mean.val[i] + range.val[i]);

		Imgproc.cvtColor(im, im, Imgproc.COLOR_HSV2BGR);
		Imgproc.rectangle(im, new Point(100, 100), new Point(150, 150), new Scalar(0, 255, 0));
		Imgproc.putText(im, ((int) mean.val[0]) + "," + ((int) mean.val[1]) + "," + ((int) mean.val[2]),
				new Point(170, 170), 1, 1, new Scalar(0, 255, 0));
	}

	private static final float smallest_area = 600.0f;

	private static MatOfPoint bigCont(Mat im) {
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(im, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		double maxArea = smallest_area;
		MatOfPoint biggestContour = null;
		for (MatOfPoint contour : contours) {
			if (contour.elemSize() > 0) {
				MatOfPoint2f thing = new MatOfPoint2f(contour.toArray());
				RotatedRect box = Imgproc.minAreaRect(thing);
				if (box != null) {
					Size size = box.size;
					double area = size.width * size.height;
					if (area > maxArea) {
						maxArea = area;
						biggestContour = contour;
					}
				}
			}
		}
		return biggestContour;
	}

	private Mat cap = new Mat();
	private ImagePanel panel;

	private volatile boolean cal = true;

	private void initCam() {
		capture = new VideoCapture();
		capture.open(0);
		if (capture.isOpened()) {
			Runnable frameGrabber = new Runnable() {

				@Override
				public void run() {
					capture.read(cap);

					if (cal) {
						calibrate(cap);
					} else {
						hand(cap);
					}
					Imgproc.putText(cap, ((int) range.val[0]) + "," + ((int) range.val[1]) + "," + ((int) range.val[2]),
							new Point(0, 15), 1, 1, new Scalar(0, 255, 0));

					BufferedImage buffer;
					if (panel.image == null) {
						buffer = new BufferedImage(cap.width(), cap.height(), BufferedImage.TYPE_3BYTE_BGR);
						panel.image = buffer;
					}
					buffer = panel.image;
					byte[] data = ((DataBufferByte) buffer.getRaster().getDataBuffer()).getData();
					cap.get(0, 0, data);
					frame.repaint();

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
		frame.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {

				boolean flag = false;
				int change = 10;
				if (e.isShiftDown())
					change = 1;
				if (e.isAltDown())
					change = 100;
				if (e.isControlDown()) {
					if (e.getKeyCode() == KeyEvent.VK_H) {
						flag = true;

						range.val[0] -= change;
					}
					if (e.getKeyCode() == KeyEvent.VK_S) {
						flag = true;
						range.val[1] -= change;
					}
					if (e.getKeyCode() == KeyEvent.VK_V) {
						flag = true;
						range.val[2] -= change;
					}
				} else {
					if (e.getKeyCode() == KeyEvent.VK_H) {
						flag = true;

						range.val[0] += change;
					}
					if (e.getKeyCode() == KeyEvent.VK_S) {
						flag = true;
						range.val[1] += change;
					}
					if (e.getKeyCode() == KeyEvent.VK_V) {
						flag = true;
						range.val[2] += change;
					}
				}

				if (flag) {
					int i;
					i = 0;
					lower.val[i] = Math.max(0, mean.val[i] - range.val[i]);
					i = 1;
					lower.val[i] = Math.max(0, mean.val[i] - range.val[i]);
					i = 2;
					lower.val[i] = Math.max(0, mean.val[i] - range.val[i]);
					i = 0;
					upper.val[i] = Math.min(179, mean.val[i] + range.val[i]);
					i = 1;
					upper.val[i] = Math.min(255, mean.val[i] + range.val[i]);
					i = 2;
					upper.val[i] = Math.min(255, mean.val[i] + range.val[i]);
				}

				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					cal = !cal;
				}
			}
		});

		panel = new ImagePanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);

	}

}
