package practice.samay.ordermanagementsystem.cache.backend;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisJsonCacheBackend implements JsonCacheBackend {

	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public Optional<String> get(String key) {
		return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
	}

	@Override
	public void put(String key, String value) {
		stringRedisTemplate.opsForValue().set(key, value, Duration.ofMinutes(30));
	}

	@Override
	public void evict(String key) {
		stringRedisTemplate.delete(key);
	}
}