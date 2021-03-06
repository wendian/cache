package cache.replacement;

import cache.exception.EvictionNotPossible;
import cache.subcache.CacheSet;

public class LeastRecentlyUsed<K, V> implements ReplacementAlgorithm<K, V> {

    private HashQueue<K> queue;

    public LeastRecentlyUsed() {
        reset();
    }

    @Override
    public V evict(CacheSet<K, V> cacheSet) throws EvictionNotPossible {
        if (cacheSet.isEmpty() || queue.isEmpty()) {
            throw new EvictionNotPossible("No entries to evict.");
        }

        K top = queue.popFirst().getId();
        return cacheSet.remove(top);
    }

    @Override
    public void notifyDelete(K key) {
        queue.remove(key);
    }

    @Override
    public void notifyAccess(K key) {
        queue.remove(key);
        queue.push(key);
    }

    @Override
    public void reset() {
        queue = new HashQueue<>();
    }
}
