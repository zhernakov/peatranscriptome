package ngsanalyser.blaster;

import blastdata.BlastedSequenceList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ngsdata.NGSRecord;

public class BLASTManager {
    private final int timeinterval = 250;
    private final int threadnumber;
    private int threadinwork = 0;
    private final BlastedSequenceList resultstorage;
    private final ExecutorService executor;

    public BLASTManager(int threadnumber, BlastedSequenceList resultstorage) {
        this.threadnumber = threadnumber;
        this.resultstorage = resultstorage; 
        executor = Executors.newFixedThreadPool(threadnumber);
    }

    synchronized public void startNewBLAST(NGSRecord record) {
        try {
            while (threadinwork >= threadnumber)
                wait();
            startBlast(record);
        } catch (InterruptedException ex) {
            
        }
    }

    private void startBlast(NGSRecord record) {
        final BLASTQuery query = new BLASTQuery(this, record);
        executor.execute(query);
        ++threadinwork;
        try {
            Thread.sleep(timeinterval);
        } catch (InterruptedException ex) {
            Logger.getLogger(BLASTManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    synchronized public void blastFinished(NGSRecord record) {
        if (record.getBlastResult() == null) {
            startBlast(record);
        } else {
            resultstorage.setBlastResult(record);
            --threadinwork;
            notify();
        }
    }

    synchronized public void shutdown() {
        executor.shutdown();
    }
}
