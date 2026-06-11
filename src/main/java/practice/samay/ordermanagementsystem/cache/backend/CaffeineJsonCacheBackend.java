package practice.samay.ordermanagementsystem.cache.backend;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class CaffeineJsonCacheBackend implements JsonCacheBackend {

	private final Cache<String, String> cache = Caffeine.newBuilder()
			.maximumSize(1_000)
			.expireAfterWrite(Duration.ofMinutes(15))
			.build();

	@Override
	public Optional<String> get(String key) {
		return Optional.ofNullable(cache.getIfPresent(key));
	}

	@Override
	public void put(String key, String value) {
		cache.put(key, value);
	}

	@Override
	public void evict(String key) {
		cache.invalidate(key);
	}
}