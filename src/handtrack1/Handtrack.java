package handtrack1;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import handtrack1.IFootageIn.FootageEndedException;
import handtrack1.caching.MappedCacheManager;
import handtrack1.resources.IConsumer;
import handtrack1.resources.ResourceManager;
import handtrack1.settings.IConfigurable;
import handtrack1.settings.SettingsNode;
import handtrack1.settings.SettingsPersistence;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

public class Handtrack implements IConsumer, IConfigurable {
	private JFrame frame;
	private JComboBox<DisplayState> displayStateSelector;

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
		frame.setBounds(100, 100, 450, 323);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		outputPanel = new ImagePanel(640, 480, BufferedImage.TYPE_3BYTE_BGR);
		frame.getContentPane().add(outputPanel, BorderLayout.CENTER);

		JPanel controlPanel = new JPanel();
		frame.getContentPane().add(controlPanel, BorderLayout.SOUTH);
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		displayStateSelector = new JComboBox<DisplayState>();
		displayStateSelector.setModel(new DefaultComboBoxModel<DisplayState>(DisplayState.values()));
		displayStateSelector.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				displayState = (DisplayState) displayStateSelector.getSelectedItem();
			}
		});
		
		lblMode = new JLabel("Mode:");
		controlPanel.add(lblMode);
		displayStateSelector.setSelectedIndex(2);
		controlPanel.add(displayStateSelector);

		JButton btnLoadSettings = new JButton("Load Settings");
		btnLoadSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						settingsManager.load(fc.getSelectedFile().getAbsolutePath());
						loadSettings(settingsManager.getRootNode().get(settings_name));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		separator = new JSeparator();
		controlPanel.add(separator);
		controlPanel.add(btnLoadSettings);

		JButton btnSaveSettings = new JButton("Save Settings");
		btnSaveSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						settingsManager.getRootNode().put(settings_name, getSettingsString());
						settingsManager.save(fc.getSelectedFile().getAbsolutePath());
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		});
		controlPanel.add(btnSaveSettings);
	}

	private static enum DisplayState {
		hand, calibrate, pause
	}

	private static final String settings_name = "handtrack";

	private ImagePanel outputPanel;
	private ScheduledExecutorService timer;

	private DisplayState displayState;

	// resources
	private MappedCacheManager<Mat> opencvMatCache;
	private SettingsPersistence settingsManager;

	// resource management
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

	// toolchain management
	private Map<String, Object> toolchain;
	private JLabel lblMode;
	private JSeparator separator;

	private void initCV() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// initialize resource framework
		resourceManager = new ResourceManager();
		resourceManager.addResource("SettingsPersistence", new SettingsPersistence());
		resourceManager.addResource("MappedCacheManager<Mat>", new MappedCacheManager<Mat>());
		resourceManager.addResource("MappedCacheManager<List<MatOfPoint>>", new MappedCacheManager<List<MatOfPoint>>());
		resourceConsumers = new ArrayList<>();

		// initialize toolchain framework
		toolchain = new HashMap<>();

		// create toolchain objects
		footageIn = new OpenCVFootageIn();
		skinDetector = new HSVSkinDetector();
		handContourFinder = new BiggestContour();
		convexityDefectFinder = new ConvexityDefects();

		footageOut = outputPanel;

		// link toolchain objects to toolchain framework
		toolchain.put("footageIn", footageIn);
		toolchain.put("skinDetector", skinDetector);
		toolchain.put("handContourFinder", handContourFinder);
		toolchain.put("convexityDefectFinder", convexityDefectFinder);

		toolchain.put("footageOut", footageOut);

		resourceConsumers.add(this);

		// only link toolchain objects if they need resources
		addResourceUser(footageIn);
		addResourceUser(skinDetector);
		addResourceUser(handContourFinder);
		addResourceUser(convexityDefectFinder);
		addResourceUser(fingerFinder);
		addResourceUser(handInfoFinder);
		addResourceUser(footageOut);

		// link consumers to resources
		for (IConsumer consumer : resourceConsumers) {
			consumer.loadResources(resourceManager);
		}

		// open input
		footageIn.open();

		// set display state to default
		displayState = (DisplayState) displayStateSelector.getSelectedItem();

		// start render loop
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
		}
	}

	private void doCVHand() {
		Mat frame = opencvMatCache.getObject("captured frame");
		Mat skinImage = opencvMatCache.getObject("skin binary image");
		skinDetector.filterSkin(frame, skinImage);
		try {
			footageIn.getFrame(frame);
		} catch (FootageEndedException e) {
			e.printStackTrace();
			displayState = DisplayState.pause;
			return;
		}
		footageOut.frameOut(frame);
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
	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		opencvMatCache = resourceManager.getResource("MappedCacheManager<Mat>");
		settingsManager = resourceManager.getResource("SettingsPersistence");
		opencvMatCache.setReference("captured frame", new Mat());
		opencvMatCache.setReference("skin binary image", new Mat());
	}

	private void addResourceUser(Object consumer) {
		if (consumer instanceof IConsumer)
			resourceConsumers.add((IConsumer) consumer);
	}

	@Override
	public String getSettingsString() {
		SettingsNode node = new SettingsNode();
		for (String key : toolchain.keySet()) {
			Object value = toolchain.get(key);
			if (value instanceof IConfigurable) {
				node.put(key, ((IConfigurable) value).getSettingsString());
			}
		}
		return node.getSettingsString();
	}

	@Override
	public void loadSettings(String settings) {
		SettingsNode node = new SettingsNode();
		node.loadSettings(settings);
		for (String key : node.getSettings()) {
			String value = node.get(key);
			((IConfigurable) toolchain.get(key)).loadSettings(value);
		}
	}
}
