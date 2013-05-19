package ngsanalyser.processing;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.DataBaseResponseException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSCollectable;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.ngsdata.NGSRecordsExcWriter;
import ngsanalyser.processor.HitsAnalyzer;
import ngsanalyser.processor.MultiBLASTer;
import ngsanalyser.processor.Storager;
import ngsanalyser.processor.StringTree;

public class BlastAnalyseStorage {
    private final Run run;
    private final NGSCollectable source;
    
    private final StringTree stored = new StringTree();

    private final NGSRecordsExcWriter failedstorage;

    private final MultiBLASTer blaster;
    private final HitsAnalyzer analyser;
    private final Storager storager;
    

    public BlastAnalyseStorage(Run run, NGSCollectable source) throws DataBaseResponseException, SQLException {
        this.run = run;
        this.source = source;
        
        DBService.INSTANCE.getStoragedSequences(run, stored);
        
        failedstorage = new NGSRecordsExcWriter(run.getTitle());
        
        storager = new Storager(null, failedstorage, 2, 20, run);
        analyser = new HitsAnalyzer(storager, failedstorage, 40, 1e-25);
        blaster = new MultiBLASTer(analyser, failedstorage, 50, 20);
    }

    public void startProcessing() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                NGSRecord record;
                while ((record = source.getNGSRecord()) != null) {
                    if (!stored.isRecordStored(record.recordid)) {
                        blaster.addNGSRecord(record);
                    }
                }
                blaster.terminate();
            }
        })).start();
    }

    public void startMonitoring() {
        final Thread monitor = new Thread(new Runnable(){

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BlastAnalyseStorage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Processor\tThreads\tWaiting, ms\tSuccessful\tFailed");
                    blaster.printReport();
                    analyser.printReport();
                    storager.printReport();
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }
}
