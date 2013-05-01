package ngsanalyser.blaster;

import blastdata.BlastedSequenceList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.biojava3.sequencing.io.fastq.Fastq;

public class BLASTManager {
    private final int threadnumber;
    private int threadinwork = 0;
    private final BlastedSequenceList resultstorage;
    private final ExecutorService executor;

    public BLASTManager(int threadnumber, BlastedSequenceList resultstorage) {
        this.threadnumber = threadnumber;
        this.resultstorage = resultstorage; 
        executor = Executors.newFixedThreadPool(threadnumber);
    }

    synchronized public void startNewBLAST(Fastq fastq) {
        try {
            while (threadinwork >= threadnumber)
                wait();
            startBlast(fastq);
        } catch (InterruptedException ex) {
            
        }
    }

    private void startBlast(Fastq fastq) {
        final BLASTQuery query = new BLASTQuery(this, fastq);
        executor.execute(query);
        ++threadinwork;
    }

    synchronized public void blastFinished(Fastq query) {
        --threadinwork;
        notify();
    }

    synchronized public void shutdown() {
        executor.shutdown();
    }
}
