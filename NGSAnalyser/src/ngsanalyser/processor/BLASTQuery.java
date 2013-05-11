package ngsanalyser.processor;

import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ncbiservice.NCBIService;
import ngsanalyser.ngsdata.NGSRecord;

class BLASTQuery implements Runnable {
    private final BLASTer processor;
    private final NGSRecord record;

    BLASTQuery(BLASTer processor, NGSRecord record) {
        this.processor = processor;
        this.record = record;
    }

    NGSRecord getRecord() {
        return record;
    }

    @Override
    public void run() {
        try {
//            System.out.println("BLAST for read " + record.recordid + " started");
            record.setBLASTHits(NCBIService.INSTANCE.blast(record.sequence));
//            System.out.println("BLAST for read " + record.recordid + " finished successfully");
            processor.threadSuccessfullyFinished(this);
        } catch (Exception ex) {
            Logger.getLogger(BLASTQuery.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("BLAST for read " + record.recordid + " faied");
            processor.threadProcessingFailed(this, ex);
        }
    }
    
}
