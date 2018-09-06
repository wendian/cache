package cache;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.List;

import cache.exception.CacheMiss;
import cache.exception.EvictionNotPossible;
import cache.hash.HashFunction;
import cache.hash.NullSafeHashFunction;
import cache.replacement.LeastRecentlyUsed;
import cache.replacement.ReplacementAlgorithm;
import cache.subcache.CacheSet;
import cache.subcache.SubCacheFactory;
import cache.subcache.SubCacheFactory.SubCacheType;

/**
 * Implementation of the Cache interface. This implementation uses a hash
 * function to retrieve the correct cache set from its list of cache sets. Most
 * of the function implementations are a pass through that call the matching
 * CacheSet class method. It includes two builder classes and is extendable to
 * perform specific tasks such as logging upon cache misses.
 *
 * @param <K>
 *            the type of keys maintained by the cache
 * @param <V>
 *            the type of mapped values
 */
public class NWaySetAssociativeCache<K, V> implements Cache<K, V> {

    private int totalSets;
    private int blockSize;
    private long hits = 0L;
    private long misses = 0L;

    private List<CacheSet<K, V>> sets;
    private HashFunction hashFunction;

    private String replacementAlgorithmName;

    /**
     * Constructs an empty NWaySetAssociativeCache, requires a functioning injected
     * List of CacheSets as well as an instance of a HashFunction and a
     * ReplacementAlgorithmType.
     *
     * @param sets
     *            a list of implementations of CacheSet, must not be null or empty.
     *            Note that this class will assume the sets are of the same size.
     * @param hashFunction
     *            an implementation of HashFunction, it is used to access the list
     *            when given a key
     * @throws IllegalArgumentException
     *             if the given CacheSet List is null or empty or if the
     *             HashFunction is null
     */
    public NWaySetAssociativeCache(List<CacheSet<K, V>> sets, HashFunction hashFunction) {
        if (isNull(sets) || sets.size() < 1) {
            throw new IllegalArgumentException("A list of cache sets was never given");
        }
        if (isNull(hashFunction)) {
            throw new IllegalArgumentException("A hashFunction was never given");
        }
        this.hashFunction = hashFunction;
        this.totalSets = sets.size();
        blockSize = sets.get(0).maxSize();
        this.sets = sets;
    }

    private int indexOf(Object key) {
        return Math.abs(hashFunction.hash(key) % totalSets);
    }

    private CacheSet<K, V> getCacheSet(Object key) {
        return sets.get(indexOf(key));
    }

    @Override
    public V put(K key, V value) {
        CacheSet<K, V> set = getCacheSet(key);
        V oldValue = null;
        try {
            oldValue = set.put(key, value);
        } catch (EvictionNotPossible e) {
            onEvictionNotPossible(e);
        }
        return oldValue;
    }

    /**
     * Does nothing, user may implement this stub for any desired purpose when an
     * eviction is not possible.
     *
     * @param e
     *            the exception that was the cause of this function call
     */
    protected void onEvictionNotPossible(EvictionNotPossible e) {
    }

    @Override
    public boolean containsKey(Object key) {
        CacheSet<K, V> set = getCacheSet(key);
        return set.containsKey(key);
    }

    @Override
    public V get(Object key) {
        CacheSet<K, V> set = getCacheSet(key);
        V value = null;
        try {
            value = set.get(key);
            onCacheHit(key);
        } catch (CacheMiss e) {
            onCacheMiss(key);
        }
        return value;
    }

    @Override
    public V remove(Object key) {
        CacheSet<K, V> set = getCacheSet(key);
        return set.remove(key);
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int maxSize() {
        return blockSize * totalSets;
    }

    @Override
    public int size() {
        int size = 0;
        for (CacheSet<K, V> set : sets) {
            size += set.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void clear() {
        sets.forEach(s -> s.clear());
        hits = 0;
        misses = 0;
    }

    @Override
    public String getReplacementAlgorithmName() {
        if (isNull(replacementAlgorithmName)) {
            return "UNKNOWN";
        } else {
            return replacementAlgorithmName;
        }
    }

    /**
     * Meant to set the replacement algorithm name, though there is no guarantee the
     * cache sets are using whatever is put here. Using the basic Builder class does
     * guarantee whatever was set here is used throughout.
     *
     * @param replacementAlgorithmName
     *            the name of the replacement algorithm, hopefully
     */
    protected void setReplacementAlgorithmName(String replacementAlgorithmName) {
        this.replacementAlgorithmName = replacementAlgorithmName;
    }

    /**
     * Called by cacheHit(). This is thread safe and only increments a hit counter.
     */
    protected synchronized void onHit() {
        hits++;
    }

    public long getHits() {
        return hits;
    }

    /**
     * Called by cacheMiss(). This is thread safe and only increments a miss
     * counter.
     */
    protected synchronized void onMiss() {
        misses++;
    }

    public long getMisses() {
        return misses;
    }

    /**
     * Called when there is a cache hit, i.e. a get() call is successful. This is
     * thread not safe and calls onCacheHit().
     * 
     * @param key
     *            the key that caused the hit
     */
    protected void onCacheHit(Object key) {
        onHit();
    }

    /**
     * Called when there is a cache miss, i.e. a get() call is unsuccessful. This is
     * thread not safe and calls onCacheMiss().
     * 
     * @param key
     *            the key that caused the miss
     */
    protected void onCacheMiss(Object key) {
        onMiss();
    }

    public void setHashFunction(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    /**
     * Simple Builder class for NWaySetAssociativeCache. Accepts enums to setup the
     * replacement algorithm and the cache set type.
     *
     * @param <K>
     *            the type of keys maintained by the cache to be built
     * @param <V>
     *            the type of mapped values
     */
    public static class Builder<K, V> {

        private int blockSize = 4;
        private int totalSets = 10;
        private SubCacheType subCacheType;
        private HashFunction hashFunction;
        private ReplacementAlgorithm<K, V> replacementAlgorithm;

        public Builder() {
        }

        /**
         * Creates a new instance of the NWaySetAssociativeCache.
         */
        public NWaySetAssociativeCache<K, V> build() {
            if (isNull(subCacheType)) {
                subCacheType = SubCacheType.HASH_MAP_CACHE_SET;
            }
            if (isNull(hashFunction)) {
                hashFunction = new NullSafeHashFunction();
            }
            if (isNull(replacementAlgorithm)) {
                replacementAlgorithm = new LeastRecentlyUsed<>();
            }
            List<CacheSet<K, V>> sets = new ArrayList<>();
            for (int i = 0; i < totalSets; i++) {
                CacheSet<K, V> cacheSet = SubCacheFactory.get(subCacheType, blockSize);
                cacheSet.setReplacementAlgorithm(replacementAlgorithm);
                sets.add(cacheSet);
            }
            NWaySetAssociativeCache<K, V> cache = new NWaySetAssociativeCache<>(sets, hashFunction);
            cache.setReplacementAlgorithmName(replacementAlgorithm.getClass().getSimpleName());
            return new NWaySetAssociativeCache<>(sets, hashFunction);
        }

        /**
         * Sets the type of set to be used for this cache
         * 
         * @param subCacheType
         *            the set type to be used for this cache, default is
         *            HASH_MAP_CACHE_SET
         */
        public Builder<K, V> setSubCacheType(SubCacheType subCacheType) {
            this.subCacheType = subCacheType;
            return this;
        }

        /**
         * Sets the total number of sets to be used for this cache
         * 
         * @param totalSets
         *            the total number of sets in this cache must be greater than 0,
         *            default is 10
         */
        public Builder<K, V> setTotalSets(int totalSets) {
            if (totalSets <= 0) {
                throw new IllegalArgumentException("Sets limit must be greater than 0");
            }
            this.totalSets = totalSets;
            return this;
        }

        /**
         * Sets the maximum number of blocks each set may have
         * 
         * @param blockSize
         *            the maximum to be set, must be greater than 0, default is 4
         */
        public Builder<K, V> setBlockSize(int blockSize) {
            if (blockSize <= 0) {
                throw new IllegalArgumentException("Block limit must be greater than 0");
            }
            this.blockSize = blockSize;
            return this;
        }

        /**
         * Sets the type of replacement algorithm to be used for this cache
         * 
         * @param algorithm
         *            the replacement algorithm type to be used for this cache, default
         *            is LeastRecentlyUsed
         */
        public Builder<K, V> setReplacementAlgorithm(ReplacementAlgorithm<K, V> algorithm) {
            replacementAlgorithm = algorithm;
            return this;
        }

        /**
         * Sets the type of hash function to be used for this cache
         * 
         * @param hashFunction
         *            the hash function type to be used for this cache, default is
         *            NullSafeCacheFunction
         */
        public Builder<K, V> setHashFunction(HashFunction hashFunction) {
            this.hashFunction = hashFunction;
            return this;
        }
    }

}
