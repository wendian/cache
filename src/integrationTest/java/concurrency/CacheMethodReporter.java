package concurrency;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import cache.exception.CacheMiss;
import cache.exception.EvictionNotPossible;
import cache.subcache.CacheSet;

public class CacheMethodReporter implements Runnable {
    private CacheSet<String, String> cacheSet;
    private Observer observer;
    private ConcurrentCacheMethod method;
    private static List<String> keys;
    private Random random;

    public static void setKeys(List<String> newKeys) {
        keys = newKeys;
    }
    
    public CacheMethodReporter(CacheSet<String, String> cacheSet, ConcurrentCacheMethod method, Observer observer) {
        this.cacheSet = cacheSet;
        this.method = method;
        this.observer = observer;
        random = new Random();
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            notifyObserverBegin();
            if (!performMethod()) {
                throw new RuntimeException("Failed to execute method: " + method.toString());
            }
            notifyObserverEnd();
        }
    }
    
    private synchronized String randomKey() {
        String key;
        if (!keys.isEmpty()) {
            key = keys.get(Math.abs(random.nextInt() % (keys.size() - 1)));
        } else {
            key = UUID.randomUUID().toString();
            keys.add(key);
        }
        return key;
    }
    
    private synchronized void removeKey(String key) {
        keys.remove(key);
    }
    
    private synchronized void clearKeys() {
        keys.clear();
    }

    private boolean performMethod() {
        String key;
        String message = "";
        boolean success = true;
        switch (method) {
        case PUT:
            key = randomKey();
            try {
                cacheSet.put(key, key);
            } catch (EvictionNotPossible e) {
                message = "Could not evict key: " + key;
                e.printStackTrace();
                success = false;
            }
            break;
        case GET:
            key = randomKey();
            try {
                cacheSet.get(key);
            } catch (CacheMiss e) {
                message = "Could not get key: " + key;
                e.printStackTrace();
                success = false;
            }
            break;
        case CHECK:
            key = randomKey();
            cacheSet.containsKey(key);
            break;
        case REMOVE:
            key = randomKey();
            removeKey(key);
            cacheSet.remove(key);
            break;
        case CLEAR:
            clearKeys();
            cacheSet.clear();
            break;
        default:
            message = "Failed to recognize command: " + method.toString();
            success = false;
            break;
        }
        if (!success) {
            System.err.println(message);
        }
        return success;
    }

    private void notifyObserverBegin() {
        observer.report(method, Thread.currentThread().getId(), true);
    }

    private void notifyObserverEnd() {
        observer.report(method, Thread.currentThread().getId(), false);
    }
}
