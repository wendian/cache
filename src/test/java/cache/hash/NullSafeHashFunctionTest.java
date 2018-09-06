package cache.hash;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NullSafeHashFunctionTest {

    private NullSafeHashFunction hashFunction = new NullSafeHashFunction();
    
    @Test
    public void testNull() {
        assertEquals(0, hashFunction.hash(null));
    }

}
