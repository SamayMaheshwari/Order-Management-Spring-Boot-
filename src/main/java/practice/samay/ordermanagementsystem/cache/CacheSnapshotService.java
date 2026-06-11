package practice.samay.ordermanagementsystem.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import practice.samay.ordermanagementsystem.cache.backend.JsonCacheBackend;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheSnapshotService {

	private final ObjectMapper objectMapper;

	public <T> Optional<T> get(JsonCacheBackend backend, String key, Class<T> valueType) {
		return backend.get(key).flatMap(value -> deserialize(value, valueType));
	}

	public <T> Optional<T> get(JsonCacheBackend backend, String key, TypeReference<T> typeReference) {
		return backend.get(key).flatMap(value -> deserialize(value, typeReference));
	}

	public void put(JsonCacheBackend backend, String key, Object value) {
		backend.put(key, serialize(value));
	}

	public void evict(JsonCacheBackend backend, String key) {
		backend.evict(key);
	}

	private String serialize(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to serialize cache value", exception);
		}
	}

	private <T> Optional<T> deserialize(String value, Class<T> valueType) {
		try {
			return Optional.of(objectMapper.readValue(value, valueType));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	private <T> Optional<T> deserialize(String value, TypeReference<T> typeReference) {
		try {
			return Optional.of(objectMapper.readValue(value, typeReference));
		} catch (Exception exception) {
			return Optional.empty();
		}
	}
}