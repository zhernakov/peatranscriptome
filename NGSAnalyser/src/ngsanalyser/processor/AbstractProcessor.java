package ngsanalyser.processor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessesManager;

public abstract class AbstractProcessor implements NGSAddible {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final List<Runnable> threads = new LinkedList<>();

    private final NGSAddible resultstorage;
    private final NGSAddible failedstorage;

    private int threadnumber;
    private int threadinwork = 0;

    protected AbstractProcessor(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        this.resultstorage = resultstorage;
        this.failedstorage = failedstorage;
        this.threadnumber = threadnumber;
    }
    
    protected synchronized void startNewThread(Runnable thread) {
        try {
            while (threadinwork >= threadnumber) {
                wait();
            }
            executor.execute(thread);
            threads.add(thread);
            ++threadinwork;
        } catch (InterruptedException ex) {
            //TODO
            Logger.getLogger(ProcessesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void restartThread(Runnable thread) {
        executor.execute(thread);
    }
    
    protected synchronized void eliminateThread(Runnable thread) {
        threads.remove(thread);
        --threadinwork;
        notify();
    }
    
    protected void recordProcessed(NGSRecord record) {
        resultstorage.addNGSRecord(record);
    }
    
    protected void recordsProcessed (Collection<NGSRecord> records) {
        resultstorage.addNGSRecordsCollection(records);
    }
    
    protected void recordProcessingFailed(NGSRecord record) {
        failedstorage.addNGSRecord(record);
    }
    
    protected void recordsProcessingFailed (Collection<NGSRecord> records) {
        failedstorage.addNGSRecordsCollection(records);
    }

    @Override
    public synchronized void terminate() {
        while (threadinwork != 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                //TODO
                Logger.getLogger(ProcessesManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        executor.shutdown();
        failedstorage.terminate();
        resultstorage.terminate();
    }

    @Override
    public int getNumber() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
