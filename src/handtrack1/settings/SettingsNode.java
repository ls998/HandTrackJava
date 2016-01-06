package handtrack1.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SettingsNode implements IConfigurable {

	private Map<String, String> map;

	private static final char openchar = '{';
	private static final char closechar = '}';
	private static final char escape = '\\';
	private static final String block = "##";

	public SettingsNode() {
		map = new HashMap<String, String>();
	}

	public void put(String key, String value) {
		if (key.contains(openchar + "") || key.contains(closechar + "") || key.contains(escape + ""))
			throw new IllegalArgumentException("Key contains invalid characters");
		map.put(key, value);
	}

	public void remove(String key) {
		map.remove(key);
	}

	public String get(String key) {
		return map.get(key);
	}

	public Set<String> getSettings() {
		return map.keySet();
	}

	@Override
	public String getSettingsString() {
		StringBuilder sb = new StringBuilder();
		for (String key : map.keySet()) {
			String value = map.get(key);
			sb.append(key);
			sb.append(openchar);
			value = value.replace(escape + "", escape + "" + escape);
			value = value.replace(openchar + "", escape + "" + openchar);
			value = value.replace(closechar + "", escape + "" + closechar);
			sb.append(value);
			sb.append(closechar);
		}
		return sb.toString();
	}

	@Override
	public void loadSettings(String settings) {
		settings = settings.replace(escape + "" + escape, escape + "");
		while (true) {
			String searchString = settings.replace(escape + "" + openchar, block).replace(escape + "" + closechar,
					block);
			int openbracketIdx = searchString.indexOf(openchar);
			int closebracketIdx = searchString.indexOf(closechar);
			if (openbracketIdx == -1 && closebracketIdx != -1)
				throw new IllegalArgumentException("Unexpected closing bracket");
			if (openbracketIdx != -1 && closebracketIdx == -1)
				throw new IllegalArgumentException("Expected closing bracket");
			if (openbracketIdx == -1 && closebracketIdx == -1)
				break;

			String key = settings.substring(0, openbracketIdx);
			String value = settings.substring(openbracketIdx + 1, closebracketIdx)
					.replace(escape + "" + openchar, openchar + "").replace(escape + "" + closechar, closechar + "");
			map.put(key, value);
			if (closebracketIdx + 1 >= settings.length())
				break;
			settings = settings.substring(closebracketIdx + 1);
		}
	}

}
