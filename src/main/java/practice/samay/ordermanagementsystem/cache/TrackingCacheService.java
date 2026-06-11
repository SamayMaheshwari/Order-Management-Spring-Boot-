package practice.samay.ordermanagementsystem.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import practice.samay.ordermanagementsystem.cache.backend.RedisJsonCacheBackend;
import practice.samay.ordermanagementsystem.dto.response.TrackingResponse;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrackingCacheService {

	private static final String TRACKING_BY_ID_KEY = "cache:tracking:id:";
	private static final String TRACKING_BY_SHIPMENT_ID_KEY = "cache:tracking:shipment:";

	private final CacheSnapshotService cacheSnapshotService;
	private final RedisJsonCacheBackend redisJsonCacheBackend;

	public Optional<TrackingResponse> getById(Long id) {
		return cacheSnapshotService.get(redisJsonCacheBackend, TRACKING_BY_ID_KEY + id, TrackingResponse.class);
	}

	public Optional<List<TrackingResponse>> getByShipmentId(Long shipmentId) {
		return cacheSnapshotService.get(redisJsonCacheBackend, TRACKING_BY_SHIPMENT_ID_KEY + shipmentId, new TypeReference<List<TrackingResponse>>() {});
	}

	public void cacheSnapshot(TrackingResponse trackingResponse) {
		cacheSnapshotService.put(redisJsonCacheBackend, TRACKING_BY_ID_KEY + trackingResponse.getId(), trackingResponse);
		evictByShipmentId(trackingResponse.getShipmentId());
	}

	public void cacheByShipmentId(Long shipmentId, List<TrackingResponse> trackingResponses) {
		cacheSnapshotService.put(redisJsonCacheBackend, TRACKING_BY_SHIPMENT_ID_KEY + shipmentId, trackingResponses);
	}

	public void evictByShipmentId(Long shipmentId) {
		cacheSnapshotService.evict(redisJsonCacheBackend, TRACKING_BY_SHIPMENT_ID_KEY + shipmentId);
	}
}