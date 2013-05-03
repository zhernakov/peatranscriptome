package ngsanalyser.processes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

abstract public class ProcessManager {
    protected final int threadnumber;
    protected int threadinwork = 0;
    private final NGSAddible resultstorage;
    protected final ExecutorService executor = Executors.newCachedThreadPool();

    public ProcessManager(int threadnumber, NGSAddible resultstorage) {
        this.threadnumber = threadnumber;
        this.resultstorage = resultstorage;
    }

    abstract public void startProcess(NGSRecord record);
    abstract public void finishProcess(NGSRecord record);
    
    synchronized protected void ProcessSuccessful(NGSRecord record) {
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
