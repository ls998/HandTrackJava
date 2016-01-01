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
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
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
		initCV();
	}

	private Mat kernel = new Mat();

	private Scalar lower = new Scalar(0, 0, 0);
	private Scalar upper = new Scalar(20, 255, 255);
	private Scalar range = new Scalar(60, 60, 250);
	private Scalar mean = new Scalar(0, 0, 0);

	private static final int scale = 1;

	private void hand(Mat im) {
		// resize to for faster speed
		Size sz = new Size(im.width() / scale, im.height() / scale);

		Imgproc.resize(im, im, sz);

		// convert to hsv colour space
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2HSV);

		// filter skin
		Mat binImg = new Mat();
		Core.inRange(im, lower, upper, binImg);

		// DESTROY SPECKSSSQ!!!!
		// Imgproc.morphologyEx(binImg, binImg, Imgproc.MORPH_OPEN, kernel);
		// Imgproc.morphologyEx(binImg, binImg, Imgproc.MORPH_OPEN, kernel);
		Imgproc.morphologyEx(binImg, binImg, Imgproc.MORPH_CLOSE, kernel);

		MatOfPoint2f bigContour = bigCont(binImg);

		contInf(bigContour, scale);

		fingers(bigContour, scale);

		Imgproc.cvtColor(binImg, im, Imgproc.COLOR_GRAY2BGR);

		if (biggest != null) {
			ArrayList<MatOfPoint> derp = new ArrayList<>();
			derp.add(biggest);
			Imgproc.drawContours(im, derp, 0, new Scalar(255, 0, 0));
		}
		Point pt2 = new Point(cogPt.x + Math.sin(contourangle) * 40, cogPt.y + Math.cos(contourangle) * 40);
		Imgproc.line(im, cogPt, pt2, new Scalar(0, 255, 255));
		Imgproc.rectangle(im, cogPt, cogPt, new Scalar(0, 0, 255));

		sz = new Size(im.width() * scale, im.height() * scale);

		Imgproc.resize(im, im, sz);
	}

	private static final int max_points = 20;

	private Point[] tipPts, foldPts;
	private double[] depths;

	private void fingers(MatOfPoint2f bigContour, int scale) {
		MatOfPoint2f t = new MatOfPoint2f();
		Imgproc.approxPolyDP(bigContour, t, 3, true);
		MatOfPoint approxContour = new MatOfPoint(t.toArray());

		MatOfInt hull = new MatOfInt();

		Imgproc.convexHull(approxContour, hull, false);

		MatOfInt4 defects = new MatOfInt4();
		Imgproc.convexityDefects(approxContour, hull, defects);

		long defectsTotal = defects.total();
		if (defectsTotal > max_points) {
			System.out.println("Processing " + max_points + " defect pts");
			defectsTotal = max_points;
		}

		// copy defect information from defects sequence into arrays
		for (int i = 0; i < defectsTotal; i++) {
			double[] dat = defects.get(0, i);

			double[] startdat = approxContour.get(0, (int) dat[0]);
			Point startPt = new Point(startdat[0], startdat[1]);
			tipPts[i] = new Point((int) Math.round(startPt.x * scale), (int) Math.round(startPt.y * scale));
			// array contains coords of the fingertips

			double[] enddat = approxContour.get(0, (int) dat[1]);
			Point endPt = new Point(enddat[0], enddat[1]);

			double[] depthdat = approxContour.get(0, (int) dat[2]);
			Point depthPt = new Point(depthdat[0], depthdat[1]);
			foldPts[i] = new Point((int) Math.round(depthPt.x * scale), (int) Math.round(depthPt.y * scale));
			// array contains coords of the skin fold between fingers

			depths[i] = dat[3] * scale;
			// array contains distances from tips to folds
		}

		reduceTips(defectsTotal, tipPts, foldPts, depths);
	}

	private void reduceTips(long defectsTotal, Point[] tipPts2, Point[] foldPts2, double[] depths2) {
		
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
	private MatOfPoint biggest;

	private MatOfPoint2f bigCont(Mat im) {
		// get all contours
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(im, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		// find biggest
		double maxArea = smallest_area;
		MatOfPoint2f biggestContour = null;

		for (MatOfPoint contour : contours) {
			// makes sure isn't empty contour
			if (contour.elemSize() > 0) {
				// calcuate size of contour
				MatOfPoint2f thing = new MatOfPoint2f(contour.toArray());
				RotatedRect box = Imgproc.minAreaRect(thing);
				if (box != null) {
					Size size = box.size;
					double area = size.width * size.height;
					if (area > maxArea) {
						maxArea = area;
						biggestContour = thing;
						biggest = contour;
					}
				}
			}
		}
		return biggestContour;
	}

	private Point cogPt;

	private int contourangle;

	private ArrayList<Point> fingerTips;

	private void contInf(MatOfPoint2f contour, int scale) {
		Moments moments = Imgproc.moments(contour, true);
		double m00 = moments.m00;
		double m10 = moments.m10;
		double m01 = moments.m01;
		if (m00 != 0) { // calculate center
			cogPt.x = (int) Math.round(m10 / m00) * scale;
			cogPt.y = (int) Math.round(m01 / m00) * scale;
		}

		// calculate angle
		double m11 = moments.m11;
		double m20 = moments.m20;
		double m02 = moments.m02;
		contourangle = calculateTilt(m11, m20, m02);

		if (fingerTips.size() > 0) {
			int yTotal = 0;
			for (Point pt : fingerTips)
				yTotal += pt.y;
			int avgYFinger = yTotal / fingerTips.size();
			if (avgYFinger > cogPt.y) // fingers below COG
				contourangle += 180;
		}
		contourangle = 180 - contourangle;
	}

	private int calculateTilt(double m11, double m20, double m02) {
		double diff = m20 - m02;
		if (diff == 0) {
			if (m11 == 0)
				return 0;
			else if (m11 > 0)
				return 45;
			else // m11 < 0
				return -45;
		}

		double theta = 0.5 * Math.atan2(2 * m11, diff);
		int tilt = (int) Math.round(Math.toDegrees(theta));

		if ((diff > 0) && (m11 == 0))
			return 0;
		else if ((diff < 0) && (m11 == 0))
			return -90;
		else if ((diff > 0) && (m11 > 0)) // 0 to 45 degrees
			return tilt;
		else if ((diff > 0) && (m11 < 0)) // -45 to 0
			return (180 + tilt); // change to counter-clockwise angle
		else if ((diff < 0) && (m11 > 0)) // 45 to 90
			return tilt;
		else if ((diff < 0) && (m11 < 0)) // -90 to -45
			return (180 + tilt); // change to counter-clockwise angle

		System.out.println("Error in moments for tilt angle");
		return 0;
	}

	private Mat cap = new Mat();
	private ImagePanel panel;

	private volatile boolean cal = true;

	private void initCV() {
		tipPts = new Point[max_points];
		foldPts = new Point[max_points];
		depths = new double[max_points];
		fingerTips = new ArrayList<>();
		cogPt = new Point();
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
