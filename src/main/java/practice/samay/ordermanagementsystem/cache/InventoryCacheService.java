package practice.samay.ordermanagementsystem.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import practice.samay.ordermanagementsystem.cache.backend.RedisJsonCacheBackend;
import practice.samay.ordermanagementsystem.model.Inventory;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryCacheService {

	private static final String INVENTORY_BY_ID_KEY = "cache:inventory:id:";
	private static final String INVENTORY_BY_PRODUCT_CODE_KEY = "cache:inventory:product:";
	private static final String INVENTORY_ALL_KEY = "cache:inventory:all";

	private final CacheSnapshotService cacheSnapshotService;
	private final RedisJsonCacheBackend redisJsonCacheBackend;

	public Optional<Inventory> getById(Long id) {
		return cacheSnapshotService.get(redisJsonCacheBackend, INVENTORY_BY_ID_KEY + id, Inventory.class);
	}

	public Optional<Inventory> getByProductCode(String productCode) {
		return cacheSnapshotService.get(redisJsonCacheBackend, INVENTORY_BY_PRODUCT_CODE_KEY + productCode, Inventory.class);
	}

	public Optional<List<Inventory>> getAll() {
		return cacheSnapshotService.get(redisJsonCacheBackend, INVENTORY_ALL_KEY, new TypeReference<List<Inventory>>() {});
	}

	public void cacheSnapshot(Inventory inventory) {
		cacheSnapshotService.put(redisJsonCacheBackend, INVENTORY_BY_ID_KEY + inventory.getId(), inventory);
		cacheSnapshotService.put(redisJsonCacheBackend, INVENTORY_BY_PRODUCT_CODE_KEY + inventory.getProductCode(), inventory);
	}

	public void cacheAll(List<Inventory> inventories) {
		cacheSnapshotService.put(redisJsonCacheBackend, INVENTORY_ALL_KEY, inventories);
	}

	public void evictAll() {
		cacheSnapshotService.evict(redisJsonCacheBackend, INVENTORY_ALL_KEY);
	}
}