package cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import cache.hash.NullSafeHashFunction;
import cache.replacement.LeastRecentlyUsed;
import cache.replacement.MostRecentlyUsed;
import cache.subcache.SubCacheFactory.SubCacheType;

public class ReplacementTest {
    private static int MAX_SETS = 1;
    private static int MAX_BLOCKS_PER_SET = 5;

    private Cache<String, String> cache;

    @Test
    public void testOLRU() {
        NWaySetAssociativeCache.Builder<String, String> builder = new NWaySetAssociativeCache.Builder<>();
        builder.setTotalSets(MAX_SETS).setSubCacheType(SubCacheType.HASH_MAP_CACHE_SET)
                .setBlockSize(MAX_BLOCKS_PER_SET).setReplacementAlgorithm(new LeastRecentlyUsed<>())
                .setHashFunction(new NullSafeHashFunction());
        cache = builder.build();

        String keyPrefix = "key";

        for (int i = 0; i < MAX_BLOCKS_PER_SET; i++) {
            String key = keyPrefix + i;
            assertNull(cache.put(key, key));
        }
        String notLRU = cache.get(keyPrefix + 0);
        String newKey = "new";
        String evicted = cache.put(newKey, newKey);
        assertNotNull(evicted);
        assertEquals(keyPrefix + 1, evicted);

        for (int i = 0; i < MAX_BLOCKS_PER_SET - 2; i++) {
            String key = newKey + i;
            assertNotEquals(notLRU, cache.put(key, key));
        }
        assertEquals(notLRU, cache.put(newKey + "x", newKey + "x"));

        assertNotNull(cache.remove(newKey + 2));
        
        assertEquals(MAX_BLOCKS_PER_SET - 1, cache.size());
        assertNull(cache.put(newKey + "a", newKey + "a"));
        assertNotNull(cache.put(newKey + "b", newKey + "b"));
    }
    
    
    @Test
    public void testOMRU() {
        NWaySetAssociativeCache.Builder<String, String> builder = new NWaySetAssociativeCache.Builder<>();
        builder.setTotalSets(MAX_SETS).setSubCacheType(SubCacheType.HASH_MAP_CACHE_SET)
                .setBlockSize(MAX_BLOCKS_PER_SET).setReplacementAlgorithm(new MostRecentlyUsed<>())
                .setHashFunction(new NullSafeHashFunction());
        cache = builder.build();

        String keyPrefix = "key";

        for (int i = 0; i < MAX_BLOCKS_PER_SET; i++) {
            String key = keyPrefix + i;
            assertNull(cache.put(key, key));
        }
        String isMRU = cache.get(keyPrefix + (MAX_BLOCKS_PER_SET - 1));
        String newKey = "new";
        String evicted = cache.put(newKey, newKey);
        assertEquals(isMRU, evicted);

        isMRU = newKey;
        for (int i = 0; i < MAX_BLOCKS_PER_SET; i++) {
            String key = newKey + i;
            assertEquals(isMRU, cache.put(key, key));
            isMRU = key;
        }
    }

}
