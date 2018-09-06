package cache.replacement;

import cache.exception.EvictionNotPossible;
import cache.subcache.CacheSet;

public interface ReplacementAlgorithm<K, V> {

    /**
     * Chooses one entry from the blocks to remove, the choice is up to the
     * implementation
     *
     * @param cacheSet
     *            the CacheSet that has the candidates for eviction
     * @return the entry value that was chosen for eviction
     * @throws EvictionNotPossible
     *             if for any reason, the algorithm cannot evict a block
     */
    V evict(CacheSet<K, V> cacheSet) throws EvictionNotPossible;

    /**
     * If the algorithm follows an observer pattern, this may be used to notify the
     * algorithm of the read/write of an entry from the cache
     *
     * @param key
     *            the key of the entry that was just accessed
     */
    void notifyAccess(K key);

    /**
     * If the algorithm follows an observer pattern, this may be used to notify the
     * algorithm of the deletion of an entry from the cache
     *
     * @param key
     *            the key of the entry that was just deleted
     */
    void notifyDelete(K key);

    /**
     * Resets the state of the algorithm, if any
     */
    void reset();

}
