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
		uppertmp.put(hsetting, "0");
		uppertmp.put(ssetting, "0");
		uppertmp.put(vsetting, "0");
		SettingsNode lowertmp = new SettingsNode();
		lowertmp.put(hsetting, "0");
		lowertmp.put(ssetting, "0");
		lowertmp.put(vsetting, "0");
		settingsNode.put(lsetting, lowertmp.getSettingsString());
		settingsNode.put(usetting, uppertmp.getSettingsString());
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
