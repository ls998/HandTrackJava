package test;

import handtrack1.settings.SettingsNode;

public class Settings {

	private static final String hsetting = "hue";
	private static final String ssetting = "sat";
	private static final String vsetting = "val";

	private static final String lsetting = "lower";
	private static final String usetting = "upper";

	public static void main(String[] args) {
		SettingsNode settingsNode = new SettingsNode();
		SettingsNode uppertmp = new SettingsNode();
		uppertmp.write(hsetting, "0");
		uppertmp.write(ssetting, "0");
		uppertmp.write(vsetting, "0");
		SettingsNode lowertmp = new SettingsNode();
		lowertmp.write(hsetting, "0");
		lowertmp.write(ssetting, "0");
		lowertmp.write(vsetting, "0");
		settingsNode.write(lsetting, lowertmp.getSettingsString());
		settingsNode.write(usetting, uppertmp.getSettingsString());
		System.out.println(settingsNode.getSettingsString());

		SettingsNode derp = new SettingsNode();
		derp.loadSettings(settingsNode.getSettingsString());

		String upperstr = derp.get(usetting);
		String lowerstr = derp.get(lsetting);
		System.out.println(upperstr);
		System.out.println(lowerstr);

		uppertmp.loadSettings(upperstr);
		lowertmp.loadSettings(lowerstr);

		System.out.println(uppertmp.get(hsetting));
		System.out.println(uppertmp.get(ssetting));
		System.out.println(uppertmp.get(vsetting));
		System.out.println(lowertmp.get(hsetting));
		System.out.println(lowertmp.get(ssetting));
		System.out.println(lowertmp.get(vsetting));
	}

}
