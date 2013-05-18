package ngsanalyser.processor;

import java.sql.SQLException;
import java.util.Set;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.NoDataBaseResponseException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSCollectable;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.ngsdata.NGSRecordsWriter;

public class Processing {
    private final Run run;
    private final NGSCollectable source;
    
    private final StringTree stored = new StringTree();

    private final NGSRecordsWriter failedstorage = new NGSRecordsWriter("failed.txt");

    private final MultiBLASTer blaster;
    private final HitsAnalyzer analyser;
    private final Storager storager;
    

    public Processing(Run run, NGSCollectable source) throws NoDataBaseResponseException, SQLException {
        this.run = run;
        this.source = source;
        
         DBService.INSTANCE.getStoragedSequences(run, stored);
        
        storager = new Storager(null, failedstorage, 2, 7, run);
        analyser = new HitsAnalyzer(storager, failedstorage, 2, 1e-25);
        blaster = new MultiBLASTer(analyser, failedstorage, 4, 20);
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

}
