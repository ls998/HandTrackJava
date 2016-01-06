package handtrack1;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import handtrack1.settings.IConfigurable;
import handtrack1.settings.SettingsNode;

public class HSVSkinDetector implements ISkinDetector, IConfigurable {

	public Scalar HSVUpper = new Scalar(0, 0, 0);
	public Scalar HSVLower = new Scalar(0, 0, 0);

	private static final String hsetting = "hue";
	private static final String ssetting = "sat";
	private static final String vsetting = "val";

	private static final String lsetting = "lower";
	private static final String usetting = "upper";

	@Override
	public void filterSkin(Mat image, Mat filteredBinaryImage) {
		Core.inRange(image, HSVLower, HSVUpper, filteredBinaryImage);
	}

	@Override
	public String getSettingsString() {
		SettingsNode settingsNode = new SettingsNode();
		SettingsNode uppertmp = new SettingsNode();
		uppertmp.put(hsetting, Double.toString(HSVUpper.val[0]));
		uppertmp.put(ssetting, Double.toString(HSVUpper.val[1]));
		uppertmp.put(vsetting, Double.toString(HSVUpper.val[2]));
		SettingsNode lowertmp = new SettingsNode();
		lowertmp.put(hsetting, Double.toString(HSVLower.val[0]));
		lowertmp.put(ssetting, Double.toString(HSVLower.val[1]));
		lowertmp.put(vsetting, Double.toString(HSVLower.val[2]));
		settingsNode.put(lsetting, lowertmp.getSettingsString());
		settingsNode.put(usetting, uppertmp.getSettingsString());
		return settingsNode.getSettingsString();
	}

	@Override
	public void loadSettings(String settings) {
		SettingsNode settingsNode = new SettingsNode();
		settingsNode.loadSettings(settings);
		String upperstr = settingsNode.get(usetting);
		String lowerstr = settingsNode.get(lsetting);

		SettingsNode uppertmp = new SettingsNode();
		SettingsNode lowertmp = new SettingsNode();
		uppertmp.loadSettings(upperstr);
		lowertmp.loadSettings(lowerstr);

		double hu = Double.parseDouble(uppertmp.get(hsetting));
		double su = Double.parseDouble(uppertmp.get(ssetting));
		double vu = Double.parseDouble(uppertmp.get(vsetting));

		double hl = Double.parseDouble(lowertmp.get(hsetting));
		double sl = Double.parseDouble(lowertmp.get(hsetting));
		double vl = Double.parseDouble(lowertmp.get(hsetting));

		HSVLower = new Scalar(hl, sl, vl);
		HSVUpper = new Scalar(hu, su, vu);
	}

}
