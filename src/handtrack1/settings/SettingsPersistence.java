package handtrack1.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class SettingsPersistence {
	private SettingsNode rootNode;

	public SettingsPersistence() {
		rootNode = new SettingsNode();
	}

	public SettingsNode getRootNode() {
		return rootNode;
	}

	public void load(String file) throws IOException {
		InputStream is = new FileInputStream(file);
		byte[] buffer = new byte[256];
		StringBuilder sb = new StringBuilder();
		while (true) {
			int i = is.read(buffer, 0, 256);
			if (i == -1)
				break;
			sb.append(new String(buffer));
		}
		rootNode.loadSettings(sb.toString());
		is.close();
	}

	public void save(String file) throws FileNotFoundException {
		PrintStream ps = new PrintStream(new FileOutputStream(file));
		ps.print(rootNode.getSettingsString());
		ps.close();
	}

}
