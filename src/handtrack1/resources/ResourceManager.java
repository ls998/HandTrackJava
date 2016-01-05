package handtrack1.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResourceManager {
	private Map<String, Object> resourceProviders;

	public ResourceManager() {
		resourceProviders = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public <T> T getResource(String resourcename) {
		Object resource = resourceProviders.get(resourcename);
		if (resource == null)
			throw new IllegalArgumentException("Resource does not exist");
		return (T) resource;
	}

	public void addResource(String resourcename, Object resourceProvider) {
		resourceProviders.put(resourcename, resourceProvider);
	}

	public Set<String> getResourceNames() {
		return resourceProviders.keySet();
	}
}
