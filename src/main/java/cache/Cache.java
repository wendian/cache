package cache;

public interface Cache<K, V> {
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
     */
    V put(K key, V value);

    /**
     * Checks the cache if the given key is associated to a value
     *
     * @param key
     *            the key to be checked
     * @return true if the key is associated with a value, false otherwise
     */
    boolean containsKey(Object key);

    /**
     * Returns the value to which the key is associated, or null if there is no key
     * in the cache
     *
     * @param key
     *            the key with which the desired value is associated
     * @return the value associated with the key, or null if there is no key for
     *         that value
     */
    V get(Object key);

    /**
     * Removes the association for the specified key from the cache if the key is
     * present.
     *
     * @param key
     *            the key for removing the key-value pair from this cache
     * @return the previous value associated with the given key, or null if there
     *         was no key in the cache
     */
    V remove(Object key);

    /**
     * Removes all entries from this cache.
     */
    void clear();

    /**
     * @return true if there are no entries in this cache, false other wise
     */
    boolean isEmpty();

    /**
     * @return the total number of key-value pairs in this cache
     */
    int size();

    /**
     * @return the maximum number of key-value pairs this cache can hold at a time
     */
    int maxSize();

    /**
     * @return a String of the name of the replacement algorithm this cache is using
     */
    String getReplacementAlgorithmName();

}
