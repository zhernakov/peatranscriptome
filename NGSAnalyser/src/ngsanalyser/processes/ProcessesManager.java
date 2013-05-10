package ngsanalyser.processes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

abstract public class ProcessesManager {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final NGSAddible resultstorage;
    private final NGSAddible failedstorage;
    private int threadnumber;
    private int threadinwork = 0;

    public ProcessesManager(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        this.resultstorage = resultstorage;
        this.failedstorage = failedstorage;
        this.threadnumber = threadnumber;
    }

    abstract public void processRecord(NGSRecord record);
    abstract public void recordProcessed(NGSRecord record);

    synchronized protected void startNewThread(Runnable thread) {
        try {
            while (threadinwork >= threadnumber) {
                wait();
            }
            executor.execute(thread);
            ++threadinwork;
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcessesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    };
    
    protected void restartThread(Runnable thread) {
        executor.execute(thread);
    }
    
    synchronized protected void recordSuccessfullyProcesed(NGSRecord record) {
        resultstorage.addNGSRecord(record);
        if (--threadinwork == 0 && executor.isShutdown()) {
            resultstorage.terminate();
            System.out.println(resultstorage.getNumber() + " records were added to stotage");
        }
        notify();
    }
    
    synchronized protected void recordCanNotBeProcessed(NGSRecord record) {
        failedstorage.addNGSRecord(record);
        --threadinwork;
        notify();
    }

    public void shutdown() {
        while (threadinwork != 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(ProcessesManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        executor.shutdown();
        resultstorage.terminate();
        failedstorage.terminate();
    }
}
