package cache.subcache;

import static java.util.Objects.isNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cache.exception.CacheMiss;
import cache.exception.EvictionNotPossible;
import cache.replacement.ReplacementAlgorithm;

public class HashMapCacheSet<K, V> implements CacheSet<K, V> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private int blockSize;
    private ReplacementAlgorithm<K, V> replacementAlgorithm;

    private Map<K, CacheBlockEntry<K, V>> blocks;

    public HashMapCacheSet(int totalBlocks) {
        this.blockSize = totalBlocks;
        blocks = new HashMap<>();
    }

    @Override
    public V put(K key, V value) throws EvictionNotPossible {
        V oldValue = null;
        writeLock.lock();
        try {
            CacheBlockEntry<K, V> existingEntryForKey = getBlock(key);
            if (isNull(existingEntryForKey)) {
                if (blocks.size() == blockSize) {
                    oldValue = evictOneEntry();
                }
                CacheBlockEntry<K, V> newEntry = new CacheBlockEntry<K, V>(key, value);
                blocks.put(key, newEntry);
            } else {
                oldValue = existingEntryForKey.setValue(value);
            }
            replacementAlgorithm.notifyAccess(key);
        } finally {
            writeLock.unlock();
        }
        return oldValue;
    }

    private V evictOneEntry() throws EvictionNotPossible {
        if (isNull(replacementAlgorithm)) {
            throw new EvictionNotPossible("Replacement Algorithm was never set");
        }
        return replacementAlgorithm.evict(this);
    }

    private CacheBlockEntry<K, V> getBlock(Object key) {
        return blocks.containsKey(key) ? blocks.get(key) : null;
    }

    @Override
    public V get(Object key) throws CacheMiss {
        readLock.lock();
        try {
            CacheBlockEntry<K, V> entry = getBlock(key);
            if (isNull(entry)) {
                throw new CacheMiss();
            } else {
                replacementAlgorithm.notifyAccess(entry.getKey());
                return entry.getValue();
            }
        } finally {
            readLock.unlock();
        }
    }

    public Map<K, CacheBlockEntry<K, V>> getBlocks() {
        return blocks;
    }

    @Override
    public int size() {
        return blocks.size();
    }

    @Override
    public boolean containsKey(Object key) {
        readLock.lock();
        try {
            get(key);
            return true;
        } catch (CacheMiss e) {
            return false;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        V value = null;
        writeLock.lock();
        try {
            if (blocks.containsKey(key)) {
                CacheBlockEntry<K, V> entry = blocks.remove(key);
                value = entry.getValue();
                replacementAlgorithm.notifyDelete(entry.getKey());
            }
        } finally {
            writeLock.unlock();
        }
        return value;
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            blocks = new HashMap<>();
            replacementAlgorithm.reset();
        } finally {
            writeLock.unlock();
        }
    }

    public ReplacementAlgorithm<K, V> getReplacementAlgorithm() {
        return replacementAlgorithm;
    }

    public void setReplacementAlgorithm(ReplacementAlgorithm<K, V> replacementAlgorithm) {
        if (isNull(this.replacementAlgorithm)) {
            this.replacementAlgorithm = replacementAlgorithm;
        }
    }

    @Override
    public int maxSize() {
        return blockSize;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Collection<K> keys() {
        writeLock.lock();
        try {
            return blocks.keySet();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Collection<V> values() {
        Set<V> values = new HashSet<>();
        writeLock.lock();
        try {
             blocks.values().forEach(v -> values.add(v.getValue()));
             return values;
        } finally {
            writeLock.unlock();
        }
    }
    
    private static class CacheBlockEntry<K, V> {

        private K key;
        private V value;

        public CacheBlockEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

    }
}
