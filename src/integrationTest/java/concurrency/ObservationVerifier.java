package concurrency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import concurrency.ConcurrentTestObserver.Entry;
import concurrency.ConcurrentTestObserver.Job;

public class ObservationVerifier {
    private Set<Job> ongoingJobs;
    private Map<Job, Long> startTimes;
    private int ongoingReads = 0;
    private int ongoingWrites = 0;

    private Averager getAverage = new Averager();
    private Averager checkAverage = new Averager();
    private Averager putAverage = new Averager();
    private Averager removeAverage = new Averager();
    private Averager clearAverage = new Averager();

    public boolean verifyCorrectBehavior(List<Entry> entries) {
        ongoingJobs = new HashSet<>();
        startTimes = new HashMap<>();
        ongoingReads = 0;
        ongoingWrites = 0;

        for (Entry entry : entries) {
            //System.out.println(entry.toString());
            if (entry.isBegin) {
                begin(entry.job, entry.systemTimestamp);
            } else {
                end(entry.job, entry.systemTimestamp);
            }
        }

        return verifyCleanEnd();
    }

    private boolean verifyCleanEnd() {
        boolean correct = true;
        if (!ongoingJobs.isEmpty()) {
            System.err.println("Remaining ongoing jobs:");
            for (Job job : ongoingJobs) {
                System.err.println(job.toString());
            }
            correct = false;
        }
        if (ongoingReads != 0) {
            System.err.println("Number of reads did not resolve cleanly with " + ongoingReads + " remaining");
            correct = false;
        }
        if (ongoingWrites != 0) {
            System.err.println("Number of writes did not resolve cleanly with " + ongoingWrites + " remaining");
            correct = false;
        }
        return correct;
    }

    private void begin(Job job, long startTime) {
        if (ongoingJobs.contains(job)) {
            System.out.println("JOB [" + job.toString() + "] attempted to begin twice");
        } else {
            ongoingJobs.add(job);
            startTimes.put(job, startTime);
            switch (job.method) {
            case GET:
            case CHECK:
                ongoingReads++;
                break;
            default:
                ongoingWrites++;
                break;
            }
        }
    }

    private void end(Job job, long endTime) {
        switch (job.method) {
        case GET:
        case CHECK:
            if (ongoingReads == 0 || ongoingJobs.isEmpty()) {
                System.out.println("JOB [" + job.toString() + "] attempted to end read but no ongoing jobs");
            }
            ongoingReads--;
            break;
        default:
            if (ongoingWrites == 0 || ongoingJobs.isEmpty()) {
                System.out.println("JOB [" + job.toString() + "] attempted to end write but no ongoing jobs");
            }
            ongoingWrites--;
            break;
        }
        ongoingJobs.remove(job);
        long startTime = startTimes.remove(job);
        long totalTime = endTime - startTime;
        //System.out.println("Took " + totalTime + " nanoseconds");
        reportToAverager(job.method, totalTime);
    }

    private void reportToAverager(ConcurrentCacheMethod method, long totalTime) {
        switch (method) {
        case GET:
            getAverage.incTotal(totalTime);
            break;
        case CHECK:
            checkAverage.incTotal(totalTime);
            break;
        case PUT:
            putAverage.incTotal(totalTime);
            break;
        case REMOVE:
            removeAverage.incTotal(totalTime);
            break;
        case CLEAR:
            clearAverage.incTotal(totalTime);
            break;
        }
    }

    public void printAverages() {
        if (getAverage.getCount() != 0) {
            System.out.println("GET average time: " + getAverage.toString());
        }
        if (checkAverage.getCount() != 0) {
            System.out.println("CHECK average time: " + checkAverage.toString());
        }
        if (putAverage.getCount() != 0) {
            System.out.println("PUT average time: " + putAverage.toString());
        }
        if (removeAverage.getCount() != 0) {
            System.out.println("REMOVE average time: " + removeAverage.toString());
        }
        if (clearAverage.getCount() != 0) {
            System.out.println("CLEAR average time: " + clearAverage.toString());
        }
    }
}
