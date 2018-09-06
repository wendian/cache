package cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import cache.exception.CacheMiss;
import cache.exception.EvictionNotPossible;
import cache.hash.CompletelyPredictableHashFunction;
import cache.subcache.CacheSet;

@RunWith(MockitoJUnitRunner.class)
public class NWaySetAssociativeCacheTest {

    private static final int MAX_SETS = 4;

    private List<CacheSet<String, String>> cacheSet;

    @Mock
    private CacheSet<String, String> set0;

    @Mock
    private CacheSet<String, String> set1;

    @Mock
    private CacheSet<String, String> set2;

    @Mock
    private CacheSet<String, String> set3;

    private CompletelyPredictableHashFunction hashFunction = new CompletelyPredictableHashFunction();

    private NWaySetAssociativeCache<String, String> cache;

    @Before
    public void setUpNewCache() {
        cacheSet = new ArrayList<>();
        cacheSet.add(set0);
        cacheSet.add(set1);
        cacheSet.add(set2);
        cacheSet.add(set3);
        cache = new NWaySetAssociativeCache<>(cacheSet, hashFunction);
    }

    @Test
    public void testPut_nullKey() throws Exception {
        String key = null;
        String value = "value";
        hashFunction.setNextHash(0);
        when(set0.get(any())).thenReturn(value);

        assertNull(cache.put(key, value));
        assertEquals(value, cache.get(key));
        assertEquals(1, cache.getHits());
    }

    @Test
    public void testPut_noCacheMisses_allNewKeys() throws Exception {
        String keyPrefix = "key";
        String valuePrefix = "value";

        for (int i = 0; i < MAX_SETS; i++) {
            hashFunction.setNextHash(i);
            when(cacheSet.get(i).get(anyString())).thenReturn(null);
            when(cacheSet.get(i).put(anyString(), anyString())).thenReturn(null);
            assertNull(cache.put(keyPrefix + i, valuePrefix + i));
        }

        for (int i = 0; i < MAX_SETS; i++) {
            hashFunction.setNextHash(i);
            when(cacheSet.get(i).get(anyString())).thenReturn(valuePrefix + i);
            assertEquals(valuePrefix + i, cache.get(keyPrefix + i));
        }
        assertEquals(0, cache.getMisses());
        assertEquals(MAX_SETS, cache.getHits());
    }

    @Test
    public void testPut_noCacheMisses_repeatKeys() throws Exception {
        String keyPrefix = "key";
        String valuePrefix = "value";

        for (int i = 0; i < MAX_SETS; i++) {
            hashFunction.setNextHash(i);
            when(cacheSet.get(i).put(anyString(), anyString())).thenReturn(null);
            assertNull(cache.put(keyPrefix + i, valuePrefix + i));
        }

        String newValuePrefix = "newValue";
        for (int i = 0; i < MAX_SETS; i++) {
            hashFunction.setNextHash(i);
            when(cacheSet.get(i).put(anyString(), anyString())).thenReturn(valuePrefix + i);
            assertEquals(valuePrefix + i, cache.put(keyPrefix + i, newValuePrefix + i));
        }

        for (int i = 0; i < MAX_SETS; i++) {
            hashFunction.setNextHash(i);
            when(cacheSet.get(i).get(anyString())).thenReturn(newValuePrefix + i);
            String actualKey = cache.get(keyPrefix + i);
            assertEquals(newValuePrefix + i, actualKey);
            assertNotEquals(valuePrefix + i, actualKey);
        }
        assertEquals(0, cache.getMisses());
        assertEquals(MAX_SETS, cache.getHits());
    }

    @Test
    public void testPut_evictOne_notPossible() throws Exception {
        String keyPrefix = "key";
        String valuePrefix = "value";
        for (int i = 0; i < MAX_SETS; i++) {
            hashFunction.setNextHash(i);
            when(cacheSet.get(i).put(anyString(), anyString())).thenReturn(null);
            assertNull(cache.put(keyPrefix + i, valuePrefix + i));
        }
        String newKey = "newKey";

        when(set0.put(anyString(), anyString())).thenThrow(new EvictionNotPossible(""));
        hashFunction.setNextHash(0);
        assertNull(cache.put(newKey, valuePrefix));
    }

    @Test
    public void testGet_cacheMisses() throws Exception {
        String keyPrefix = "key";
        String valuePrefix = "value";
        for (int i = 0; i < MAX_SETS; i++) {
            hashFunction.setNextHash(i);
            when(cacheSet.get(i).put(anyString(), anyString())).thenReturn(null);
            assertNull(cache.put(keyPrefix + i, valuePrefix + i));
        }
        keyPrefix = "nonExistent";
        for (int i = 0; i < MAX_SETS; i++) {
            hashFunction.setNextHash(i);
            when(cacheSet.get(i).get(anyString())).thenThrow(new CacheMiss());
            assertNull(cache.get(keyPrefix + i));
        }
        assertEquals(MAX_SETS, cache.getMisses());
        assertEquals(0, cache.getHits());
    }
    
    @Test
    public void testContainsKey_cacheMisses() throws Exception {
        String key = "key";
        String value = "value";
        hashFunction.setNextHash(0);
        when(set0.put(anyString(), anyString())).thenReturn(null);
        assertNull(cache.put(key, value));
        
        when(set0.containsKey(anyString())).thenReturn(true);
        assertTrue(cache.containsKey(key));
    }

    @Test
    public void testRemove() throws Exception {
        String key = "key";
        String value = "value";
        hashFunction.setNextHash(0);
        when(set0.put(anyString(), anyString())).thenReturn(null);
        assertNull(cache.put(key, value));

        when(set0.remove(anyString())).thenReturn(value);
        assertEquals(value, cache.remove(key));
    }
}
