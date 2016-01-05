package handtrack1.caching;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MappedCacheManager<T> {
	private Map<String, T> objects;

	public MappedCacheManager() {
		objects = new HashMap<>();
	}

	public void setReference(String id, T object) {
		objects.put(id, object);
	}

	public void removeReference(String id) {
		objects.remove(id);
	}

	public T getObject(String id) {
		return objects.get(id);
	}

	public Set<String> getObjects() {
		return objects.keySet();
	}
}
