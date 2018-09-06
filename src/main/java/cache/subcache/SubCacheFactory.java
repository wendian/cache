package cache.subcache;

public class SubCacheFactory {

    public static <K, V> CacheSet<K, V> get(SubCacheType cacheType, int totalBlocks) {
       switch (cacheType) {
       default:
           return new HashMapCacheSet<>(totalBlocks);
       }
    }

    public static enum SubCacheType {
        HASH_MAP_CACHE_SET;
    }
}
