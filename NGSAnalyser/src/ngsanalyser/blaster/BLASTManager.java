package ngsanalyser.blaster;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class BLASTManager {
    private final int timeinterval = 250;
    private final int threadnumber;
    private int threadinwork = 0;
    private final NGSAddible resultstorage;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public BLASTManager(int threadnumber, NGSAddible resultstorage) {
        this.threadnumber = threadnumber;
        this.resultstorage = resultstorage; 
    }

    synchronized public void startNewBLAST(NGSRecord record) {
        try {
            while (threadinwork >= threadnumber)
                wait();
            startBLAST(record);
        } catch (InterruptedException ex) {
            
        }
    }

    private void startBLAST(NGSRecord record) {
        final BLASTQuery query = new BLASTQuery(this, record);
        executor.execute(query);
        ++threadinwork;
        try {
            Thread.sleep(timeinterval);
        } catch (InterruptedException ex) {
            Logger.getLogger(BLASTManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    synchronized public void finishBLAST(NGSRecord record) {
        if (record.getBlastResult() == null) {
            startBLAST(record);
        } else {
            resultstorage.addNGSRecord(record);
            if (--threadinwork == 0 && executor.isShutdown()) {
                resultstorage.terminate();
                System.out.println(resultstorage.getNumber() + " records were added to stotage after BLASTing");
            }
            notify();
        }
    }
    
    synchronized public void shutdown() {
        executor.shutdown();
     }
}
