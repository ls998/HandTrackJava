package handtrack1.settings;

public interface IConfigurable {
	public String getSettingsString();

	public void loadSettings(String settings);
}
