package practice.samay.ordermanagementsystem.cache.backend;

import java.util.Optional;

public interface JsonCacheBackend {

	Optional<String> get(String key);

	void put(String key, String value);

	void evict(String key);
}