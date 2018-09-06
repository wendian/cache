package cache.subcache;

import java.util.Collection;

import cache.exception.CacheMiss;
import cache.exception.EvictionNotPossible;
import cache.replacement.ReplacementAlgorithm;

public interface CacheSet<K, V> {

    void setReplacementAlgorithm(ReplacementAlgorithm<K, V> replacementAlgorithm);

    /**
     * Associates the given key with the given value.
     *
     * @param key
     *            the key to be associated with the value
     * @param value
     *            the value to be associated with the key
     * @return the previous value associated with the key, or the value associated
     *         with the entry that was evicted by the replacement algorithm, or null
     *         if the entry is new
     * @throws EvictionNotPossible
     *             if the replacement algorithm cannot evict any entry
     */
    V put(K key, V value) throws EvictionNotPossible;

    /**
     * Checks the cache if the given key is associated to a value
     *
     * @param key
     *            the key to be checked
     * @return true if the key is associated with a value, false otherwise
     */
    boolean containsKey(Object key);

    /**
     * Returns the value to which the key is associated, or throws an exception if
     * there is no entry for the key
     *
     * @param key
     *            the key with which the desired value is associated
     * @return the value associated with the key
     * @throws CacheMiss
     *             if the entry associated to the key does not exist
     */
    V get(Object key) throws CacheMiss;

    /**
     * Removes the association for the specified key from the cache if the key is
     * present
     *
     * @param key
     *            the key for removing the key-value pair from this cache
     * @return the previous value associated with the given key, or null if there
     *         was no key in the cache
     */
    V remove(Object key);

    /**
     * Removes all entries from this cache set
     */
    void clear();

    /**
     * @return true if there are no entries in this cache set, false other wise
     */
    boolean isEmpty();

    /**
     * @return the total number of key-value pairs in this cache set
     */
    int size();

    /**
     * @return the maximum number of key-value pairs this cache can hold at a time
     */
    int maxSize();

    /**
     * @return a collection of the values in this cache set
     */
    Collection<V> values();

    /**
     * @return a collection of the keys in this cache set
     */
    Collection<K> keys();

}
