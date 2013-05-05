package ngsanalyser.processes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

abstract public class ProcessManager {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final NGSAddible resultstorage;
    private final NGSAddible failedstorage;
    private final int threadnumber;
    private int threadinwork = 0;
    private Timer timer;

    public ProcessManager(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        this.resultstorage = resultstorage;
        this.failedstorage = failedstorage;
        this.threadnumber = threadnumber;
    }

    abstract public void processRecord(NGSRecord record);
    abstract public void recordProcessed(NGSRecord record);
    
    protected void setTimer(int interval) {
        timer = new Timer(interval);
    }
    
    synchronized protected void newRecordProcessing(Runnable thread) {
        try {
            while (threadinwork >= threadnumber) {
                wait();
            }
            executor.execute(thread);
            ++threadinwork;
            if (timer != null) {
                timer.start();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    };
    
    synchronized protected void restartRecordProcessing(Runnable thread) {
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
    }
    
    synchronized public void shutdown() {
        executor.shutdown();
    }
}
