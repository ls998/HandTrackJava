package handtrack1;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import handtrack1.IFootageIn.FootageEndedException;
import handtrack1.caching.MappedCacheManager;
import handtrack1.resources.IConsumer;
import handtrack1.resources.ResourceManager;
import handtrack1.settings.SettingsPersistence;

public class Handtrack implements IConsumer {
	private JFrame frame;

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

	private static enum DisplayState {
		hand, calibrate, loadfile, pause
	}

	private ImagePanel panel;
	private ScheduledExecutorService timer;

	private DisplayState displayState;

	private static final String settingsFileLocation = "";

	// resources
	private MappedCacheManager<Mat> opencvMatCache;
	private ResourceManager resourceManager;
	private List<IConsumer> resourceConsumers;

	// CV toolchain objects
	private IFootageIn footageIn;
	private ISkinDetector skinDetector;
	private IHandContourFinder handContourFinder;
	private ConvexityDefects convexityDefectFinder;
	private IFingerFinder fingerFinder;
	private IHandInfoFinder handInfoFinder;
	private IFootageOut footageOut;

	private void initCV() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		resourceConsumers = new ArrayList<>();
		resourceConsumers.add(this);

		resourceManager = new ResourceManager();
		resourceManager.addResource("persistent settings", new SettingsPersistence());
		resourceManager.addResource("opencv Mat cache", new MappedCacheManager<Mat>());
		for(IConsumer consumer:resourceConsumers){
			consumer.loadResources(resourceManager);
		}
		
		displayState = DisplayState.calibrate;

		footageOut = panel;

		footageIn = new OpenCVFootageIn();

		footageIn.open();

		this.timer = Executors.newSingleThreadScheduledExecutor();
		this.timer.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					doCV();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}, 0, 33, TimeUnit.MILLISECONDS);
	}

	private void doCV() {
		if (displayState == DisplayState.hand) {
			doCVHand();
		} else if (displayState == DisplayState.calibrate) {
			doCVCalibrate();
		} else if (displayState == DisplayState.loadfile) {
			doCVLoadFile();
		}
	}

	private void doCVHand() {

	}

	private void doCVCalibrate() {
		Mat frame = opencvMatCache.getObject("captured frame");
		try {
			footageIn.getFrame(frame);
		} catch (FootageEndedException e) {
			e.printStackTrace();
			displayState = DisplayState.pause;
			return;
		}
		footageOut.frameOut(frame);
		panel.repaint();
	}

	private void doCVLoadFile() {

	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		opencvMatCache = resourceManager.getResource("opencv Mat cache");
		opencvMatCache.setReference("captured frame", new Mat());
	}
}
