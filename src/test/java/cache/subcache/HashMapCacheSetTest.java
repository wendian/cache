package cache.subcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import cache.exception.CacheMiss;
import cache.exception.EvictionNotPossible;
import cache.replacement.ReplacementAlgorithm;

@RunWith(MockitoJUnitRunner.class)
public class HashMapCacheSetTest {

    private static final int MAX_BLOCKS = 10;

    @Mock
    private ReplacementAlgorithm<String, String> replacementAlgorithm;

    private HashMapCacheSet<String, String> cacheSet;

    @Before
    public void setUpNewCache() {
        cacheSet = new HashMapCacheSet<>(MAX_BLOCKS);
        cacheSet.setReplacementAlgorithm(replacementAlgorithm);
    }

    @Test
    public void testPut_nullKey() throws Exception {
        String key = null;
        String value = "value";
        cacheSet.put(key, value);
        verify(replacementAlgorithm, never()).evict(any());
        assertEquals(value, cacheSet.get(key));
        assertEquals(1, cacheSet.size());
    }

    @Test
    public void testPut_allNewKeys() throws Exception {
        String keyPrefix = "key";
        String valuePrefix = "value";
        for (int i = 0; i < MAX_BLOCKS; i++) {
            cacheSet.put(keyPrefix + i, valuePrefix + i);
        }
        verify(replacementAlgorithm, never()).evict(any());
        for (int i = 0; i < MAX_BLOCKS; i++) {
            assertEquals(valuePrefix + i, cacheSet.get(keyPrefix + i));
        }
        assertEquals(MAX_BLOCKS, cacheSet.size());
    }

    @Test
    public void testPut_repeatKeys() throws Exception {
        String keyPrefix = "key";
        String valuePrefix = "value";
        for (int i = 0; i < MAX_BLOCKS; i++) {
            cacheSet.put(keyPrefix + i, valuePrefix + i);
        }
        String newValuePrefix = "newValue";
        for (int i = 0; i < MAX_BLOCKS; i++) {
            cacheSet.put(keyPrefix + i, newValuePrefix + i);
        }
        verify(replacementAlgorithm, never()).evict(any());

        for (int i = 0; i < MAX_BLOCKS; i++) {
            String actualKey = cacheSet.get(keyPrefix + i);
            assertEquals(newValuePrefix + i, actualKey);
            assertNotEquals(valuePrefix + i, actualKey);
        }
    }

    @Test
    public void testPut_evictOne() throws Exception {
        String keyPrefix = "key";
        String valuePrefix = "value";
        for (int i = 0; i < MAX_BLOCKS; i++) {
            cacheSet.put(keyPrefix + i, valuePrefix + i);
        }
        verify(replacementAlgorithm, never()).evict(any());
        String newKey = "newKey";

        when(replacementAlgorithm.evict(any())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                cacheSet.getBlocks().remove(keyPrefix + 0);
                return valuePrefix + 0;
            }
        });

        String oldValue = cacheSet.put(newKey, valuePrefix);

        verify(replacementAlgorithm, times(1)).evict(any());
        assertEquals(MAX_BLOCKS, cacheSet.size());
        assertEquals(valuePrefix, cacheSet.get(newKey));
        assertEquals(valuePrefix + 0, oldValue);
    }

    @Test(expected = EvictionNotPossible.class)
    public void testPut_evictOne_notPossible() throws Exception {
        String keyPrefix = "key";
        String valuePrefix = "value";
        for (int i = 0; i < MAX_BLOCKS; i++) {
            cacheSet.put(keyPrefix + i, valuePrefix + i);
        }
        verify(replacementAlgorithm, never()).evict(any());
        String newKey = "newKey";

        when(replacementAlgorithm.evict(any())).thenThrow(new EvictionNotPossible("mock"));

        try {
            cacheSet.put(newKey, valuePrefix);
        } catch (EvictionNotPossible e) {
            verify(replacementAlgorithm, times(1)).evict(any());
            assertEquals(MAX_BLOCKS, cacheSet.size());
            for (int i = 0; i < MAX_BLOCKS; i++) {
                assertEquals(valuePrefix + i, cacheSet.get(keyPrefix + i));
            }
            assertFalse(cacheSet.containsKey(newKey));
            throw e;
        }
    }

    @Test(expected = CacheMiss.class)
    public void testGet_cacheMisses() throws Exception {
        String keyPrefix = "key";
        String valuePrefix = "value";
        for (int i = 0; i < MAX_BLOCKS; i++) {
            cacheSet.put(keyPrefix + i, valuePrefix + i);
        }
        verify(replacementAlgorithm, never()).evict(any());
        keyPrefix = "nonExistent";
        try {
            cacheSet.get(keyPrefix);
        } catch (CacheMiss e) {
            assertEquals(MAX_BLOCKS, cacheSet.size());
            throw e;
        }
    }

    @Test
    public void testRemove() throws Exception {
        String key = "key";
        String value = "value";
        cacheSet.put(key, value);
        String removedValue = cacheSet.remove(key);
        verify(replacementAlgorithm, never()).evict(any());
        assertEquals(value, removedValue);
        assertFalse(cacheSet.containsKey(key));
        assertEquals(0, cacheSet.size());
    }

    @Test
    public void testRemove_noKey() throws Exception {
        String key = "key";
        String removedValue = cacheSet.remove(key);
        verify(replacementAlgorithm, never()).evict(any());
        assertNull(removedValue);
        assertEquals(0, cacheSet.size());
    }

}
