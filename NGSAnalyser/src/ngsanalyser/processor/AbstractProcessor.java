package ngsanalyser.processor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ngsdata.NGSAddible;

public abstract class AbstractProcessor implements NGSAddible {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private int threadnumber;
    private int threadinwork = 0;

    private final int waitingsize = 20;
    private final long[] waiting = new long[waitingsize];
    private int waitingcursor = 0;
    private int startedthreads = 0;

    protected final NGSAddible resultstorage;
    protected final NGSAddible failedstorage;

    protected AbstractProcessor(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        this.resultstorage = resultstorage;
        this.failedstorage = failedstorage;
        this.threadnumber = threadnumber;
    }
    
    protected synchronized void startNewThread(Runnable thread) {
        try {
            long start = System.nanoTime();
            while (threadinwork >= threadnumber) {
                wait();
            }
            executor.execute(thread);
            ++threadinwork;
            addWaitingTime(System.nanoTime() - start);
        } catch (InterruptedException ex) {
            //TODO
            Logger.getLogger(AbstractProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void restartThread(Runnable thread) {
        executor.execute(thread);
    }
    
    protected synchronized void eliminateThread(Runnable thread) {
        --threadinwork;
        notify();
    }

    @Override
    public synchronized void terminate() {
        while (threadinwork != 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                //TODO
                Logger.getLogger(AbstractProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        executor.shutdown();
        failedstorage.terminate();
        resultstorage.terminate();
    }

    @Override
    public int getNumber() {
        return startedthreads;
    }

    public int getThreadInWork() {
        return threadinwork;
    }

    private void addWaitingTime(long t) {
        waiting[waitingcursor] = t;
        waitingcursor = (waitingcursor + 1) % waitingsize;
        ++startedthreads;
    }
    
    public long meanWaitingTime() {
        int number = startedthreads / waitingsize > 0 ? waitingsize : startedthreads;
        if (number == 0) {
            return 0;
        }
        long sum = 0;
        for (int i = 0; i < number; ++i) {
            sum += this.waiting[i];
        }
        return sum / number;
    }
}
