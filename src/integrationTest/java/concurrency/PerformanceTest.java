package concurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import cache.exception.CacheMiss;
import cache.exception.EvictionNotPossible;
import cache.replacement.LeastRecentlyUsed;
import cache.replacement.MostRecentlyUsed;
import cache.replacement.ReplacementAlgorithm;
import cache.subcache.CacheSet;
import cache.subcache.HashMapCacheSet;

public class PerformanceTest {
    private static int MAX_BLOCKS = 50000;

    @Test
    public void testMultiRead() {
        System.out.println("testMultiRead");
        System.out.println("Observer LRU: ");
        BuilderRunner builderRunner = new BuilderRunner();
        builderRunner.setMaxBlocks(MAX_BLOCKS).setReplacementAlgorithm(new LeastRecentlyUsed<>())
            .addMethodRunner(ConcurrentCacheMethod.GET)
            .addMethodRunner(ConcurrentCacheMethod.CHECK)
            .run();

        System.out.println("Observer MRU: ");
        builderRunner = new BuilderRunner();
        builderRunner.setMaxBlocks(MAX_BLOCKS).setReplacementAlgorithm(new MostRecentlyUsed<>())
            .addMethodRunner(ConcurrentCacheMethod.GET)
            .addMethodRunner(ConcurrentCacheMethod.CHECK)
            .run();
    }

    @Test
    public void testMultiReadAndWrite() {
        System.out.println("testMultiReadAndWrite");
        System.out.println("Observer LRU: ");
        BuilderRunner builderRunner = new BuilderRunner();
        builderRunner.setMaxBlocks(MAX_BLOCKS).setReplacementAlgorithm(new LeastRecentlyUsed<>())
            .addMethodRunner(ConcurrentCacheMethod.CHECK)
            .addMethodRunner(ConcurrentCacheMethod.PUT)
            .run();
        
        System.out.println("Observer MRU: ");
        builderRunner = new BuilderRunner();
        builderRunner.setMaxBlocks(MAX_BLOCKS).setReplacementAlgorithm(new MostRecentlyUsed<>())
            .addMethodRunner(ConcurrentCacheMethod.GET)
            .addMethodRunner(ConcurrentCacheMethod.CHECK)
            .addMethodRunner(ConcurrentCacheMethod.PUT)
            .run();
    }

    @Test
    public void testMultiWrite() {
        System.out.println("testMultiWrite");
        System.out.println("Observer LRU: ");
        BuilderRunner builderRunner = new BuilderRunner();
        builderRunner.setMaxBlocks(MAX_BLOCKS).setReplacementAlgorithm(new LeastRecentlyUsed<>())
            .addMethodRunner(ConcurrentCacheMethod.REMOVE)
            .addMethodRunner(ConcurrentCacheMethod.PUT)
            .run();
        
        System.out.println("Observer MRU: ");
        builderRunner = new BuilderRunner();
        builderRunner.setMaxBlocks(MAX_BLOCKS).setReplacementAlgorithm(new MostRecentlyUsed<>())
            .addMethodRunner(ConcurrentCacheMethod.REMOVE)
            .addMethodRunner(ConcurrentCacheMethod.PUT)
            .run();
    }

    private static class BuilderRunner {
        private int maxBlocks;
        private ReplacementAlgorithm<String, String> replacementAlgorithm;
        private Set<ConcurrentCacheMethod> methods = new HashSet<>();
        private ConcurrentTestObserver observer = new ConcurrentTestObserver();
        private CacheSet<String, String> cacheSet;

        public void run() {
            cacheSet = new HashMapCacheSet<>(maxBlocks);
            cacheSet.setReplacementAlgorithm(replacementAlgorithm);

            try {
                CacheMethodReporter.setKeys(addRandomKeys());
            } catch (EvictionNotPossible | CacheMiss e) {
                e.printStackTrace();
                fail("Failed to set up test");
            }

            List<Thread> pool = new ArrayList<>();
            for (ConcurrentCacheMethod method : methods) {
                pool.add(makeThread(method));
            }
            for (Thread thread : pool) {
                thread.start();
            }
            try {
                for (Thread thread : pool) {
                    thread.join(1_000_000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail("Threads did not return in time");
            }

            ObservationVerifier observationVerifier = new ObservationVerifier();
            assertTrue(observationVerifier.verifyCorrectBehavior(observer.getEntries()));
            observationVerifier.printAverages();
        }

        private List<String> addRandomKeys()
                throws EvictionNotPossible, CacheMiss {
            Random random = new Random();
            List<String> addedKeys = new ArrayList<>();

            for (int i = 0; i < MAX_BLOCKS; i++) {
                int x = random.nextInt();
                String key = Integer.toString(x);
                cacheSet.put(key, key);
                addedKeys.add(key);
            }

            for (String key : addedKeys) {
                assertEquals(key, cacheSet.get(key));
            }
            return addedKeys;
        }

        private Thread makeThread(ConcurrentCacheMethod method) {
            CacheMethodReporter reporter = new CacheMethodReporter(cacheSet, method, observer);
            return new Thread(reporter);
        }

        public BuilderRunner setMaxBlocks(int maxBlocks) {
            this.maxBlocks = maxBlocks;
            return this;
        }

        public BuilderRunner setReplacementAlgorithm(ReplacementAlgorithm<String, String> replacementAlgorithm) {
            this.replacementAlgorithm = replacementAlgorithm;
            return this;
        }

        public BuilderRunner addMethodRunner(ConcurrentCacheMethod concurrentCacheMethod) {
            methods.add(concurrentCacheMethod);
            return this;
        }

    }
}
