package ngsanalyser.processor;

import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.ncbiservice.NCBIService;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

class BLASTQuery implements Runnable {
    protected static int recordsprocessed = 0;
    protected static int recordsfailed = 0;
    
    private final BLASTer processor;
    private final NGSAddible resultstorage;
    private final NGSAddible failedstorage;
    
    private final NGSRecord record;

    public BLASTQuery(BLASTer processor, NGSAddible resultstorage, NGSAddible failedstorage, NGSRecord record) {
        this.processor = processor;
        this.resultstorage = resultstorage;
        this.failedstorage = failedstorage;
        this.record = record;
    }

    @Override
    public void run() {
        try {
//            System.out.println("BLAST for read " + record.recordid + " started");
            record.setBLASTHits(NCBIService.INSTANCE.blast(record.sequence));
//            System.out.println("BLAST for read " + record.recordid + " finished successfully");
            ++recordsprocessed;
            resultstorage.addNGSRecord(record);
            processor.eliminateThread(this);
        } catch (NoConnectionException ex) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex1) {
//                Logger.getLogger(BLASTQuery.class.getName()).log(Level.SEVERE, null, ex1);
            }
            processor.restartThread(this);
        } catch (Exception ex) {
//            Logger.getLogger(BLASTQuery.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("BLAST for read " + record.recordid + " failed");
            record.loqError(ex);
            ++recordsfailed;
            failedstorage.addNGSRecord(record);
            processor.eliminateThread(this);
        }
    }
}
