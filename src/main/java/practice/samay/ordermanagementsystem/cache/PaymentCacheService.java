package practice.samay.ordermanagementsystem.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import practice.samay.ordermanagementsystem.cache.backend.LruJsonCacheBackend;
import practice.samay.ordermanagementsystem.cache.backend.RedisJsonCacheBackend;
import practice.samay.ordermanagementsystem.dto.response.PaymentResponse;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentCacheService {

	private static final String PAYMENT_BY_ID_KEY = "cache:payment:id:";
	private static final String PAYMENT_BY_REFERENCE_KEY = "cache:payment:reference:";
	private static final String PAYMENT_BY_ORDER_ID_KEY = "cache:payment:order:";

	private final CacheSnapshotService cacheSnapshotService;
	private final LruJsonCacheBackend lruJsonCacheBackend;
	private final RedisJsonCacheBackend redisJsonCacheBackend;

	public Optional<PaymentResponse> getById(Long id) {
		return cacheSnapshotService.get(lruJsonCacheBackend, PAYMENT_BY_ID_KEY + id, PaymentResponse.class);
	}

	public Optional<PaymentResponse> getByReference(String reference) {
		return cacheSnapshotService.get(lruJsonCacheBackend, PAYMENT_BY_REFERENCE_KEY + reference, PaymentResponse.class);
	}

	public Optional<List<PaymentResponse>> getByOrderId(Long orderId) {
		return cacheSnapshotService.get(lruJsonCacheBackend, PAYMENT_BY_ORDER_ID_KEY + orderId, new TypeReference<List<PaymentResponse>>() {});
	}

	public void cacheSnapshot(PaymentResponse paymentResponse) {
		cacheSnapshotService.put(lruJsonCacheBackend, PAYMENT_BY_ID_KEY + paymentResponse.getId(), paymentResponse);
		cacheSnapshotService.put(lruJsonCacheBackend, PAYMENT_BY_REFERENCE_KEY + paymentResponse.getPaymentReference(), paymentResponse);
		cacheSnapshotService.put(redisJsonCacheBackend, PAYMENT_BY_ID_KEY + paymentResponse.getId(), paymentResponse);
		cacheSnapshotService.put(redisJsonCacheBackend, PAYMENT_BY_REFERENCE_KEY + paymentResponse.getPaymentReference(), paymentResponse);
		evictByOrderId(paymentResponse.getOrderId());
	}

	public void cacheByOrderId(Long orderId, List<PaymentResponse> payments) {
		cacheSnapshotService.put(lruJsonCacheBackend, PAYMENT_BY_ORDER_ID_KEY + orderId, payments);
		cacheSnapshotService.put(redisJsonCacheBackend, PAYMENT_BY_ORDER_ID_KEY + orderId, payments);
	}

	public void evictByOrderId(Long orderId) {
		cacheSnapshotService.evict(lruJsonCacheBackend, PAYMENT_BY_ORDER_ID_KEY + orderId);
		cacheSnapshotService.evict(redisJsonCacheBackend, PAYMENT_BY_ORDER_ID_KEY + orderId);
	}
}