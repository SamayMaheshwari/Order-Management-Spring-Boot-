package practice.samay.ordermanagementsystem.cache.backend;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class LruJsonCacheBackend implements JsonCacheBackend {

	private static final int MAX_ENTRIES = 256;

	private final Map<String, String> cache = new LinkedHashMap<>(MAX_ENTRIES, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
			return size() > MAX_ENTRIES;
		}
	};

	@Override
	public synchronized Optional<String> get(String key) {
		return Optional.ofNullable(cache.get(key));
	}

	@Override
	public synchronized void put(String key, String value) {
		cache.put(key, value);
	}

	@Override
	public synchronized void evict(String key) {
		cache.remove(key);
	}
}