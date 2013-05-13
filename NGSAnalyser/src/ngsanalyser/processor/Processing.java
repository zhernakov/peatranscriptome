package ngsanalyser.processor;

import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.NoDataBaseResponseException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSCollectable;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.ngsdata.NGSRecordsCollection;
import ngsanalyser.ngsdata.NGSRecordsWriter;

public class Processing {
    private final Run run;
    private final NGSCollectable source;
    
    private final Set<String> stored;

    private final NGSRecordsWriter failedstorage = new NGSRecordsWriter("failed.txt");
    
    private final BLASTer blaster;
    private final HitsAnalyzer analyzer;
    private final Storager storager;

    public Processing(Run run, NGSCollectable source) throws NoDataBaseResponseException, SQLException {
        this.run = run;
        this.source = source;
        
        stored = DBService.INSTANCE.getStoragedSequences(run);
        
        storager = new Storager(failedstorage, 2, run);
        analyzer = new HitsAnalyzer(storager, failedstorage, 5);
        blaster = new BLASTer(analyzer, failedstorage, 50);
    }
    
    private boolean isRecordStored(String id) {
        for (final String storedid : stored) {
            if (storedid.equals(id)) {
                return true;
            }
        }
        return false;
    }
    
    public void startProcessing() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                NGSRecord record;
                while ((record = source.getNGSRecord()) != null) {
                    if (!isRecordStored(record.recordid)) {
                        blaster.addNGSRecord(record);
                    }
                }
                blaster.terminate();
            }
        })).start();
    }
    
    public void startMonitoring() {
        final Thread monitor = new Thread(new Runnable() {

            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ex) {
                    }
                    System.out.println("Processor\tThreads\tWaiting\tProcessed\tFailed");
                    System.out.println("Blaster:\t" + 
                            blaster.getThreadInWork() + "\t"
                            + blaster.meanWaitingTime() + "\t" 
                            + BLASTQuery.recordsprocessed + "\t"
                            + BLASTQuery.recordsfailed);
                    System.out.println("HitsAnalyzer:\t" + 
                            analyzer.getThreadInWork() + "\t"
                            + analyzer.meanWaitingTime() + "\t" 
                            + HitsScan.recordsprocessed + "\t"
                            + HitsScan.recordsfailed);
                    System.out.println("Storager:\t" + 
                            storager.getThreadInWork() + "\t"
                            + storager.meanWaitingTime() + "\t" 
                            + Storaging.recordsprocessed + "\t"
                            + Storaging.recordsfailed);
                    System.out.println();
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }
}
