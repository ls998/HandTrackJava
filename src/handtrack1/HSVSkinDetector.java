package handtrack1;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import handtrack1.settings.IConfigurable;
import handtrack1.settings.SettingsNode;

public class HSVSkinDetector implements ISkinDetector, IConfigurable {

	public Scalar HSVUpper;
	public Scalar HSVLower;

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
		uppertmp.write(hsetting, Double.toString(HSVUpper.val[0]));
		uppertmp.write(ssetting, Double.toString(HSVUpper.val[1]));
		uppertmp.write(vsetting, Double.toString(HSVUpper.val[2]));
		SettingsNode lowertmp = new SettingsNode();
		lowertmp.write(hsetting, Double.toString(HSVLower.val[0]));
		lowertmp.write(ssetting, Double.toString(HSVLower.val[1]));
		lowertmp.write(vsetting, Double.toString(HSVLower.val[2]));
		settingsNode.write(lsetting, lowertmp.getSettingsString());
		settingsNode.write(usetting, uppertmp.getSettingsString());
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
