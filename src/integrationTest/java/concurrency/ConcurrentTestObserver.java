package concurrency;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentTestObserver implements Observer {

    private List<Entry> entries = new ArrayList<>();

    @Override
    public synchronized void report(ConcurrentCacheMethod method, long threadId, boolean isBegin) {
        Entry entry = new Entry(newJob(method, threadId), isBegin);
        entries.add(entry);
    }
    
    public List<Entry> getEntries() {
        return entries;
    }
    
    private Job newJob(ConcurrentCacheMethod method, long threadId) {
        Job job = new Job();
        job.method = method;
        job.threadId = threadId;
        return job;
    }

    public static class Entry {
        Job job;
        boolean isBegin;
        long systemTimestamp;
        
        public Entry(Job job, boolean isBegin) {
            this.job = job;
            this.isBegin = isBegin;
            systemTimestamp = System.nanoTime();
        }

        @Override
        public String toString() {
            return "Thread: " + job.threadId + " " + systemTimestamp + " " +  (isBegin ? "BEGIN " : "  END ")
                    + job.method.toString();
        }
    }

    public static class Job {
        ConcurrentCacheMethod method;
        long threadId;

        @Override
        public String toString() {
            return "Thread: " + threadId + " perform " + method.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((method == null) ? 0 : method.hashCode());
            result = prime * result + (int) (threadId ^ (threadId >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Job other = (Job) obj;
            if (method != other.method) {
                return false;
            }
            if (threadId != other.threadId) {
                return false;
            }
            return true;
        }
    }
}
