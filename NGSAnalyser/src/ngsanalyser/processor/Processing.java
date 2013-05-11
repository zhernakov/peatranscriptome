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
        analyzer = new HitsAnalyzer(storager, failedstorage, 10);
        blaster = new BLASTer(analyzer, failedstorage, 15);
    }

    public void startProcessing() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                NGSRecord record;
                while ((record = source.getNGSRecord()) != null) {
                    blaster.addNGSRecord(record);
                }
                blaster.terminate();
            }
        })).start();
    }
}
