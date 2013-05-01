package ngsanalyser.blaster;

import org.biojava3.sequencing.io.fastq.Fastq;

public class BLASTQuery implements Runnable {
    private final Fastq fastq;
    private final BLASTManager controller;
    
    public BLASTQuery(BLASTManager controller, Fastq fastq) {
        this.fastq = fastq;
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            final String id = fastq.getDescription();
            System.out.println("Blast for " + id + " started.");
            Thread.sleep(1000);
            System.out.println("Blast for " + id + " finished.");
        } catch (Exception e) {
            
        } finally {
            controller.blastFinished(fastq);
        }
    }

    Fastq getResult() {
        return fastq;
    }
    
}
