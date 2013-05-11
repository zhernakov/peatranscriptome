package ngsanalyser.processor;

import java.sql.SQLException;
import java.util.Set;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.NoDataBaseRespondException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSCollectable;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.ngsdata.NGSRecordsCollection;

public class Processing {
    private final Run run;
    private final NGSCollectable source;
    
    private final Set<String> stored;

    private final NGSRecordsCollection tmpstorage = new NGSRecordsCollection();
    private final NGSRecordsCollection failedstorage = new NGSRecordsCollection();
    
    private final BLASTer blaster;
    private final HitsAnalyzer analyzer;
    private final Storager storager;

    public Processing(Run run, NGSCollectable source) throws NoDataBaseRespondException, SQLException {
        this.run = run;
        this.source = source;
        
        stored = DBService.INSTANCE.getStoragedSequences(run);
        
        storager = new Storager(tmpstorage, failedstorage, 2, run);
        analyzer = new HitsAnalyzer(storager, failedstorage, 40);
        blaster = new BLASTer(analyzer, failedstorage, 120);
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
    
    public void printMeanWaitingTime() {
        System.out.println("Blaster:      " + blaster.meanWaitingTime());
        System.out.println("HitsAnalyzer: " + analyzer.meanWaitingTime());
        System.out.println("Storager:     " + storager.meanWaitingTime());
        final Runtime r = Runtime.getRuntime();
        System.out.println("free memory: " + r.freeMemory() 
                + "\t total memory: " + r.totalMemory() 
                + "\t maximal memery: " + r.maxMemory());
        System.out.println(1. - (double)r.freeMemory() / r.totalMemory());
       
    }
}
