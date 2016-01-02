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
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class Handtrack1 {

	enum FingerName {
		LITTLE, RING, MIDDLE, INDEX, THUMB, UNKNOWN;

		public FingerName getNext() {
			int nextIdx = ordinal() + 1;
			if (nextIdx == (values().length))
				nextIdx = 0;
			return values()[nextIdx];
		} // end of getNext()

		public FingerName getPrev() {
			int prevIdx = ordinal() - 1;
			if (prevIdx < 0)
				prevIdx = values().length - 1;
			return values()[prevIdx];
		} // end of getPrev()

	}

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

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 640, 480);
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

				if (e.getKeyCode() == KeyEvent.VK_1) {
					kernelmode = 1;
				}
				if (e.getKeyCode() == KeyEvent.VK_2) {
					kernelmode = 2;
				}
				if (e.getKeyCode() == KeyEvent.VK_3) {
					kernelmode = 3;
				}
				if (e.getKeyCode() == KeyEvent.VK_4) {
					kernelmode = 4;
				}
				if (e.getKeyCode() == KeyEvent.VK_5) {
					useCustomKernel = !useCustomKernel;
				}
				if (e.getKeyCode() == KeyEvent.VK_0) {
					size++;
					kernelcust = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(size, size));
				}
				if (e.getKeyCode() == KeyEvent.VK_MINUS) {
					if (size > 0)
						size--;
					kernelcust = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(size, size));
				}
				boolean flag = false;
				int change = 10;
				if (e.isShiftDown())
					change = 1;
				if (e.isAltDown()) {
					if (e.getKeyCode() == KeyEvent.VK_H) {
						viewmode = 1;
					}
					if (e.getKeyCode() == KeyEvent.VK_B) {
						viewmode = 2;
					}
					if (e.getKeyCode() == KeyEvent.VK_R) {
						viewmode = 3;
					}
				}
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

	private static final int max_points = 20;
	private static final float smallest_area = 600.0f;
	private static final int scale = 2;

	// settings
	private Scalar lower = new Scalar(0, 0, 0);
	private Scalar upper = new Scalar(20, 255, 255);
	private Scalar range = new Scalar(110, 200, 60);
	private Scalar mean = new Scalar(90, 170, 17);
	private int kernelmode;
	private boolean useCustomKernel;
	private int size = 4;
	private int viewmode = 1;

	private volatile boolean cal = true;

	private Mat kernelcust;
	private Mat kernel = new Mat();
	private Point[] tipPts, foldPts;
	private double[] depths;
	private long numPoints;
	private MatOfInt hull;
	private Point cogPt;
	private ArrayList<Point> fingerTips;
	private Mat cap = new Mat();
	private ImagePanel panel;
	private VideoCapture capture;
	private CascadeClassifier faceDetector;

	private void hand(Mat im) {
		// resize to for faster speed
		Size sz = new Size(im.width() / scale, im.height() / scale);

		Imgproc.resize(im, im, sz);

		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(im, faceDetections);

		// convert to hsv colour space
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2HSV);

		// filter skin
		Mat binImg = new Mat();
		Core.inRange(im, lower, upper, binImg);

		// destroy face

		for (Rect rect : faceDetections.toArray()) {
			Mat sel = new Mat(binImg, rect);
			sel.setTo(new Scalar(0));
		}

		// DESTROY SPECKSSSQ!!!!
		// Imgproc.erode(binImg, binImg, kernel);
		Mat k;
		if (useCustomKernel)
			k = kernelcust;
		else
			k = kernel;

		if (kernelmode == 1) {
			Imgproc.erode(binImg, binImg, k);
			Imgproc.dilate(binImg, binImg, k);
		} else if (kernelmode == 2) {
			Imgproc.morphologyEx(binImg, binImg, Imgproc.MORPH_OPEN, k);
		} else if (kernelmode == 3) {
			Imgproc.morphologyEx(binImg, binImg, Imgproc.MORPH_CLOSE, k);
		} else {
			Imgproc.morphologyEx(binImg, binImg, Imgproc.MORPH_OPEN, k);
			Imgproc.morphologyEx(binImg, binImg, Imgproc.MORPH_CLOSE, k);
		}

		// Imgproc.dilate(binImg, binImg, kernel);

		Mat cpy = new Mat();
		binImg.copyTo(cpy);

		MatOfPoint2f bigContour = bigCont(cpy);

		if (viewmode == 2) {
			Imgproc.cvtColor(binImg, im, Imgproc.COLOR_GRAY2BGR);
		}
		if (viewmode == 3) {
			Imgproc.cvtColor(im, im, Imgproc.COLOR_HSV2BGR);
		}

		if (bigContour != null) {
			contInf(bigContour);

			MatOfPoint2f t = new MatOfPoint2f();
			Imgproc.approxPolyDP(bigContour, t, 3, true);
			MatOfPoint approxContour = new MatOfPoint(t.toArray());

			toStar(approxContour);

			reduceTips();

			Imgproc.rectangle(im, cogPt, cogPt, new Scalar(0, 255, 255));

			for (Rect rect : faceDetections.toArray()) {
				Imgproc.rectangle(im, rect.br(), rect.tl(), new Scalar(0, 0, 255));
			}

			for (int i = 0; i < numPoints; i++) {
				Imgproc.circle(im, tipPts[i], 2, new Scalar(0, 0, 255));
				Imgproc.circle(im, foldPts[i], 2, new Scalar(255, 0, 0));
			}

			// nameFingers();

			int count = 0;

			for (int i = 0; i < fingerTips.size(); i++) {
				Point tip = fingerTips.get(i);

				if (tip.y > cogPt.y)
					continue;

				count++;
				Imgproc.circle(im, tip, 3, new Scalar(0, 255, 255));
			}
			Imgproc.putText(im, "" + count, new Point(0, 100), 1, 2, new Scalar(0, 0, 0));
		}
		sz = new Size(im.width() * scale, im.height() * scale);

		Imgproc.resize(im, im, sz);
	}

	private void toStar(MatOfPoint approxContour) {

		hull = new MatOfInt();

		Imgproc.convexHull(approxContour, hull, false);

		MatOfInt4 defects = new MatOfInt4();
		Imgproc.convexityDefects(approxContour, hull, defects);

		numPoints = defects.total();
		if (numPoints > max_points) {
			System.out.println("Processing " + max_points + " defect pts");
			numPoints = max_points;
		}
		// copy defect information from defects sequence into arrays
		for (int i = 0; i < numPoints; i++) {
			double[] dat = defects.get(i, 0);

			double[] startdat = approxContour.get((int) dat[0], 0);

			Point startPt = new Point(startdat[0], startdat[1]);

			tipPts[i] = new Point((int) Math.round(startPt.x), (int) Math.round(startPt.y));

			// array contains coords of the fingertips

			// double[] enddat = approxContour.get((int) dat[1], 0);
			// Point endPt = new Point(enddat[0], enddat[1]);

			double[] depthdat = approxContour.get((int) dat[2], 0);
			Point depthPt = new Point(depthdat[0], depthdat[1]);
			foldPts[i] = new Point((int) Math.round(depthPt.x), (int) Math.round(depthPt.y));
			// array contains coords of the skin fold between fingers

			depths[i] = dat[3];
			// array contains distances from tips to folds
		}
	}

	// globals
	private static final int MIN_THUMB = 120; // angle ranges
	private static final int MAX_THUMB = 200;

	private static final int MIN_INDEX = 60;
	private static final int MAX_INDEX = 120;

	// globals
	private ArrayList<FingerName> namedFingers;

	private void nameFingers() { // reset all named fingers to unknown
		namedFingers.clear();
		for (int i = 0; i < fingerTips.size(); i++)
			namedFingers.add(FingerName.UNKNOWN);

		labelThumbIndex();
		labelUnknowns();
	}

	private int angleToCOG(Point tipPt) {
		int yOffset = (int) (cogPt.y - tipPt.y); // make y positive up screen
		int xOffset = (int) (tipPt.x - cogPt.x);
		double theta = Math.atan2(yOffset, xOffset);
		int angleTip = (int) Math.round(Math.toDegrees(theta));
		return angleTip - 90;
	}

	private void labelThumbIndex() {
		boolean foundThumb = false;
		boolean foundIndex = false;
		int i = fingerTips.size() - 1;
		while ((i >= 0)) {
			int angle = angleToCOG(fingerTips.get(i));
			// check for thumb
			if ((angle <= MAX_THUMB) && (angle > MIN_THUMB) && !foundThumb) {
				namedFingers.set(i, FingerName.THUMB);
				foundThumb = true;
			}

			// check for index
			if ((angle <= MAX_INDEX) && (angle > MIN_INDEX) && !foundIndex) {
				namedFingers.set(i, FingerName.INDEX);
				foundIndex = true;
			}
			i--;
		}
	}

	private void labelUnknowns() {
		// find first named finger
		int i = 0;
		while ((i < namedFingers.size()) && (namedFingers.get(i) == FingerName.UNKNOWN))
			i++;
		if (i == namedFingers.size()) // no named fingers found, so give up
			return;

		FingerName name = namedFingers.get(i);
		labelPrev(i, name); // fill-in backwards
		labelFwd(i, name); // fill-in forwards
	} // end of labelUnknowns()

	private void labelPrev(int i, FingerName name)
	// move backwards through fingers list labelling unknown fingers
	{
		i--;
		while ((i >= 0) && (name != FingerName.UNKNOWN)) {
			if (namedFingers.get(i) == FingerName.UNKNOWN) { // unknown finger
				name = name.getPrev();
				if (!usedName(name))
					namedFingers.set(i, name);
			} else // finger is named already
				name = namedFingers.get(i);
			i--;
		}
	}

	private void labelFwd(int i, FingerName name)
	// move forward through fingers list labelling unknown fingers
	{
		i++;
		while ((i < namedFingers.size()) && (name != FingerName.UNKNOWN)) {
			if (namedFingers.get(i) == FingerName.UNKNOWN) { // unknown finger
				name = name.getNext();
				if (!usedName(name))
					namedFingers.set(i, name);
			} else // finger is named already
				name = namedFingers.get(i);
			i++;
		}
	}

	private boolean usedName(FingerName name)
	// does the fingers list contain name already?
	{
		for (FingerName fn : namedFingers)
			if (fn == name)
				return true;
		return false;
	}

	private static final int MIN_FINGER_DEPTH = 20;
	private static final int MAX_FINGER_ANGLE = 60;
	private static final int MIN_FINGER_ANGLE = 15;

	private void reduceTips() {
		fingerTips.clear();
		for (int i = 0; i < numPoints; i++) {
			if (depths[i] < MIN_FINGER_DEPTH) // defect too shallow
				continue;

			// look at fold points on either side of a tip
			int pdx = (int) ((i == 0) ? (numPoints - 1) : (i - 1)); // predecessor
																	// of i
			int sdx = (i == numPoints - 1) ? 0 : (i + 1); // successor of i

			int angle = angleBetween(tipPts[i], foldPts[pdx], foldPts[sdx]);
			if (angle >= MAX_FINGER_ANGLE)
				continue;
			// if (angle < MIN_FINGER_ANGLE)
			// continue;
			// this point is probably a fingertip, so add to list
			fingerTips.add(tipPts[i]);
		}
	}

	private int angleBetween(Point tip, Point next, Point prev)
	// calculate the angle between the tip and its neighboring folds
	// (in integer degrees)
	{
		return Math.abs((int) Math.round(Math
				.toDegrees(Math.atan2(next.x - tip.x, next.y - tip.y) - Math.atan2(prev.x - tip.x, prev.y - tip.y))));
	}

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
					}
				}
			}
		}
		return biggestContour;
	}

	private void contInf(MatOfPoint2f contour) {
		Moments moments = Imgproc.moments(contour, true);
		double m00 = moments.m00;
		double m10 = moments.m10;
		double m01 = moments.m01;
		if (m00 != 0) { // calculate center
			cogPt.x = (int) Math.round(m10 / m00);
			cogPt.y = (int) Math.round(m01 / m00);
		}
	}

	private void calibrate(Mat im) {

		java.awt.Point d = frame.getMousePosition();
		if (d == null)
			d = new java.awt.Point(100, 100);
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2HSV);
		Mat rect = new Mat(im, new Rect(new Point(d.getX(), d.getY()), new Point(d.getX() + 50, d.getY() + 50)));
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

		Imgproc.rectangle(im, new Point(d.getX(), d.getY()), new Point(d.getX() + 50, d.getY() + 50),
				new Scalar(0, 255, 0));
		Imgproc.putText(im, ((int) mean.val[0]) + "," + ((int) mean.val[1]) + "," + ((int) mean.val[2]),
				new Point(170, 170), 1, 1, new Scalar(0, 255, 0));
	}

	private void initCV() {
		namedFingers = new ArrayList<>();
		faceDetector = new CascadeClassifier(
				"C:\\Users\\sunny\\lib\\opencv-3.1.0\\sources\\data\\haarcascades\\haarcascade_frontalface_alt.xml");
		kernelcust = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(size, size));
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
					try {
						capture.read(cap);
						// Core.flip(cap, cap, 1);
						if (cal) {
							calibrate(cap);
						} else {
							hand(cap);
						}
						Imgproc.putText(cap,
								((int) range.val[0]) + "," + ((int) range.val[1]) + "," + ((int) range.val[2]),
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
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
		}
	}

}
