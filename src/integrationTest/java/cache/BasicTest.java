package cache;

import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import cache.hash.NullSafeHashFunction;
import cache.replacement.LeastRecentlyUsed;
import cache.subcache.SubCacheFactory.SubCacheType;

public class BasicTest {
    private static int MAX_SETS = 1;
    private static int MAX_BLOCKS_PER_SET = 5;

    private Cache<String, String> cache;

    @Before
    public void setUpNewCache() {
        NWaySetAssociativeCache.Builder<String, String> builder = new NWaySetAssociativeCache.Builder<>();
        builder.setTotalSets(MAX_SETS)
            .setSubCacheType(SubCacheType.HASH_MAP_CACHE_SET)
            .setBlockSize(MAX_BLOCKS_PER_SET)
            .setReplacementAlgorithm(new LeastRecentlyUsed<>())
            .setHashFunction(new NullSafeHashFunction());

        cache = builder.build();
    }

    @Test
    public void testNull() {
        String key = null;
        String value = null;
        assertNull(cache.put(key, value));
        assertTrue(cache.containsKey(key));
    }
    
    @Test
    public void testClear() {
        String keyPrefix = "key";

        for (int i = 0; i < MAX_SETS * MAX_BLOCKS_PER_SET; i++) {
            String key = keyPrefix + i;
            cache.put(key, key);
        }
        cache.clear();
        assertTrue(cache.isEmpty());
    }

    @Test
    public void testBasic_keyCollision() {
        String keyPrefix = "key";

        List<String> addedKeys = new ArrayList<>();
        List<String> removedKeys = new ArrayList<>();

        for (int i = 0; i < MAX_SETS * MAX_BLOCKS_PER_SET; i++) {
            String key = keyPrefix + i;
            String value = cache.put(key, key);
            if (isNull(value)) {
                System.out.println("Adding: " + key);
            } else {
                System.out.println("Evicted: " + value);
                removedKeys.add(value);
            }
            addedKeys.add(key);
        }
        addedKeys.removeAll(removedKeys);
        String value = "aaaa";
        for (String key : addedKeys) {
            assertEquals(key, cache.put(key, value));
            assertEquals(value, cache.get(key));
        }

        assertEquals(addedKeys.size(), cache.size());
    }

    @Test
    public void testBasic_usesEviction() {
        String keyPrefix = "key";

        Random random = new Random();
        List<String> addedKeys = new ArrayList<>();
        List<String> removedKeys = new ArrayList<>();

        for (int i = 0; i < MAX_SETS * MAX_BLOCKS_PER_SET * 2; i++) {
            int x = random.nextInt();
            String key = keyPrefix + x;
            String value = cache.put(key, key);
            if (isNull(value)) {
                System.out.println("Adding: " + key);
            } else {
                System.out.println("Evicted: " + value);
                removedKeys.add(value);
            }
            addedKeys.add(key);
        }

        for (String key : removedKeys) {
            assertFalse(cache.containsKey(key));
        }
        addedKeys.removeAll(removedKeys);
        for (String key : addedKeys) {
            assertEquals(key, cache.get(key));
        }

        assertEquals(addedKeys.size(), cache.size());
    }

    @Test
    public void testBasic_removeKeys() {
        String keyPrefix = "key";

        List<String> addedKeys = new ArrayList<>();
        List<String> removedKeys = new ArrayList<>();

        for (int i = 0; i < MAX_SETS * MAX_BLOCKS_PER_SET * 2; i++) {
            String key = keyPrefix + i;
            String value = cache.put(key, key);
            if (isNull(value)) {
                System.out.println("Adding: " + key);
            } else {
                System.out.println("Evicted: " + value);
                removedKeys.add(value);
            }
            addedKeys.add(key);
        }
        addedKeys.removeAll(removedKeys);
        for (String key : addedKeys) {
            assertEquals(key, cache.remove(key));
            assertFalse(cache.containsKey(key));
        }

        assertTrue(cache.isEmpty());
    }
}
