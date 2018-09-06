package concurrency;

public class Averager {
    private long total = 0;
    private long count = 0;

    public long getCount() {
        return count;
    }

    public void incTotal(long n) {
        total += n;
        count++;
    }

    public long getAverage() {
        return total / count;
    }
    
    @Override
    public String toString() {
        return Long.toString(getAverage());
    }
}
