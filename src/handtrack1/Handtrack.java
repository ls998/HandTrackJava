package handtrack1;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;

public class Handtrack {

	private JFrame frame;
	private ImagePanel panel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Handtrack window = new Handtrack();
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
	public Handtrack() {
		initialize();
		initCV();
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

	// CV toolchain objects
	private ISkinDetector skinDetector;
	private IHandContourFinder handContourFinder;
	private ConvexityDefects convexityDefectFinder;
	private IFingerFinder fingerFinder;
	private IHandInfoFinder handInfoFinder;

	private void initCV() {

	}

	private void doCV() {

	}

	private void doCVHand() {

	}

	private void doCVCalibrate() {

	}
}
