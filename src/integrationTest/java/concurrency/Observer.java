package concurrency;

public interface Observer {

    void report(ConcurrentCacheMethod method, long threadId, boolean isBegin);
    
}
