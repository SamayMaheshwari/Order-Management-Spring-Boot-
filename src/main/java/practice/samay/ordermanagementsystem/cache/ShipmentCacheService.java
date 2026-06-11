package practice.samay.ordermanagementsystem.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import practice.samay.ordermanagementsystem.cache.backend.CaffeineJsonCacheBackend;
import practice.samay.ordermanagementsystem.cache.backend.RedisJsonCacheBackend;
import practice.samay.ordermanagementsystem.dto.response.ShipmentResponse;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShipmentCacheService {

	private static final String SHIPMENT_BY_ID_KEY = "cache:shipment:id:";
	private static final String SHIPMENT_BY_TRACKING_KEY = "cache:shipment:tracking:";
	private static final String SHIPMENT_BY_ORDER_ID_KEY = "cache:shipment:order:";
	private static final String SHIPMENT_ALL_KEY = "cache:shipment:all";

	private final CacheSnapshotService cacheSnapshotService;
	private final CaffeineJsonCacheBackend caffeineJsonCacheBackend;
	private final RedisJsonCacheBackend redisJsonCacheBackend;

	public Optional<ShipmentResponse> getById(Long id) {
		return cacheSnapshotService.get(caffeineJsonCacheBackend, SHIPMENT_BY_ID_KEY + id, ShipmentResponse.class);
	}

	public Optional<ShipmentResponse> getByTrackingNumber(String trackingNumber) {
		return cacheSnapshotService.get(caffeineJsonCacheBackend, SHIPMENT_BY_TRACKING_KEY + trackingNumber, ShipmentResponse.class);
	}

	public Optional<List<ShipmentResponse>> getByOrderId(Long orderId) {
		return cacheSnapshotService.get(caffeineJsonCacheBackend, SHIPMENT_BY_ORDER_ID_KEY + orderId, new TypeReference<List<ShipmentResponse>>() {});
	}

	public Optional<List<ShipmentResponse>> getAll() {
		return cacheSnapshotService.get(caffeineJsonCacheBackend, SHIPMENT_ALL_KEY, new TypeReference<List<ShipmentResponse>>() {});
	}

	public void cacheSnapshot(ShipmentResponse shipmentResponse) {
		cacheSnapshotService.put(caffeineJsonCacheBackend, SHIPMENT_BY_ID_KEY + shipmentResponse.getId(), shipmentResponse);
		cacheSnapshotService.put(caffeineJsonCacheBackend, SHIPMENT_BY_TRACKING_KEY + shipmentResponse.getTrackingNumber(), shipmentResponse);
		cacheSnapshotService.put(redisJsonCacheBackend, SHIPMENT_BY_ID_KEY + shipmentResponse.getId(), shipmentResponse);
		cacheSnapshotService.put(redisJsonCacheBackend, SHIPMENT_BY_TRACKING_KEY + shipmentResponse.getTrackingNumber(), shipmentResponse);
		evictByOrderId(shipmentResponse.getOrderId());
		evictAll();
	}

	public void cacheByOrderId(Long orderId, List<ShipmentResponse> shipments) {
		cacheSnapshotService.put(caffeineJsonCacheBackend, SHIPMENT_BY_ORDER_ID_KEY + orderId, shipments);
		cacheSnapshotService.put(redisJsonCacheBackend, SHIPMENT_BY_ORDER_ID_KEY + orderId, shipments);
	}

	public void cacheAll(List<ShipmentResponse> shipments) {
		cacheSnapshotService.put(caffeineJsonCacheBackend, SHIPMENT_ALL_KEY, shipments);
		cacheSnapshotService.put(redisJsonCacheBackend, SHIPMENT_ALL_KEY, shipments);
	}

	public void evictByOrderId(Long orderId) {
		cacheSnapshotService.evict(caffeineJsonCacheBackend, SHIPMENT_BY_ORDER_ID_KEY + orderId);
		cacheSnapshotService.evict(redisJsonCacheBackend, SHIPMENT_BY_ORDER_ID_KEY + orderId);
	}

	public void evictAll() {
		cacheSnapshotService.evict(caffeineJsonCacheBackend, SHIPMENT_ALL_KEY);
		cacheSnapshotService.evict(redisJsonCacheBackend, SHIPMENT_ALL_KEY);
	}
}