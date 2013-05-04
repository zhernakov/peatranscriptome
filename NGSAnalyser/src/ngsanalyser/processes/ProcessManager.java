package ngsanalyser.processes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

abstract public class ProcessManager {
    private final int threadnumber;
    private int threadinwork = 0;
    private final NGSAddible resultstorage;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ProcessManager(int threadnumber, NGSAddible resultstorage) {
        this.threadnumber = threadnumber;
        this.resultstorage = resultstorage;
    }

    abstract public void processRecord(NGSRecord record);
    abstract public void recordProcessed(NGSRecord record);
    
    synchronized protected void processIsReady(Runnable thread) {
        try {
            while (threadinwork >= threadnumber) {
                wait();
            }
            executor.execute(thread);
            ++threadinwork;
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    };
    
    synchronized protected void restartProcess(Runnable thread) {
        executor.execute(thread);
    }
    
    synchronized protected void processSuccessfullyFinished(NGSRecord record) {
        resultstorage.addNGSRecord(record);
        if (--threadinwork == 0 && executor.isShutdown()) {
            resultstorage.terminate();
            System.out.println(resultstorage.getNumber() + " records were added to stotage after Parsing");
        }
        notify();
    }
    
    synchronized public void shutdown() {
        executor.shutdown();
    }
}
