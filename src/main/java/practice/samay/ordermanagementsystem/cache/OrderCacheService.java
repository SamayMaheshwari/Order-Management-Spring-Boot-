package practice.samay.ordermanagementsystem.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import practice.samay.ordermanagementsystem.cache.backend.RedisJsonCacheBackend;
import practice.samay.ordermanagementsystem.dto.response.OrderResponse;
import practice.samay.ordermanagementsystem.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderCacheService {

	private static final String ORDER_BY_ID_KEY = "cache:order:id:";
	private static final String ORDER_BY_NUMBER_KEY = "cache:order:number:";
	private static final String ORDER_ALL_KEY = "cache:order:all";
	private static final String ORDER_BY_STATUS_KEY = "cache:order:status:";

	private final CacheSnapshotService cacheSnapshotService;
	private final RedisJsonCacheBackend redisJsonCacheBackend;

	public Optional<OrderResponse> getById(Long id) {
		return cacheSnapshotService.get(redisJsonCacheBackend, ORDER_BY_ID_KEY + id, OrderResponse.class);
	}

	public Optional<OrderResponse> getByOrderNumber(String orderNumber) {
		return cacheSnapshotService.get(redisJsonCacheBackend, ORDER_BY_NUMBER_KEY + orderNumber, OrderResponse.class);
	}

	public Optional<List<OrderResponse>> getAll() {
		return cacheSnapshotService.get(redisJsonCacheBackend, ORDER_ALL_KEY, new TypeReference<List<OrderResponse>>() {});
	}

	public Optional<List<OrderResponse>> getByStatus(OrderStatus status) {
		return cacheSnapshotService.get(redisJsonCacheBackend, ORDER_BY_STATUS_KEY + status.name(), new TypeReference<List<OrderResponse>>() {});
	}

	public void cacheSnapshot(OrderResponse orderResponse) {
		cacheSnapshotService.put(redisJsonCacheBackend, ORDER_BY_ID_KEY + orderResponse.getId(), orderResponse);
		cacheSnapshotService.put(redisJsonCacheBackend, ORDER_BY_NUMBER_KEY + orderResponse.getOrderNumber(), orderResponse);
	}

	public void cacheAll(List<OrderResponse> orders) {
		cacheSnapshotService.put(redisJsonCacheBackend, ORDER_ALL_KEY, orders);
	}

	public void cacheByStatus(OrderStatus status, List<OrderResponse> orders) {
		cacheSnapshotService.put(redisJsonCacheBackend, ORDER_BY_STATUS_KEY + status.name(), orders);
	}

	public void evictListCaches() {
		cacheSnapshotService.evict(redisJsonCacheBackend, ORDER_ALL_KEY);
		for (OrderStatus status : OrderStatus.values()) {
			cacheSnapshotService.evict(redisJsonCacheBackend, ORDER_BY_STATUS_KEY + status.name());
		}
	}
}