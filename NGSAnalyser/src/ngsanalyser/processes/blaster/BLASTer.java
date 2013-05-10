package ngsanalyser.processes.blaster;

import java.sql.SQLException;
import java.util.Set;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.NoDataBaseRespondException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSCollectable;
import ngsanalyser.ngsdata.NGSRecord;

public class BLASTer {
    private final NGSCollectable ngsfile;
    private final BLASTManager manager;
    private final Set<String> indatabase;

    public BLASTer(
            NGSCollectable source, NGSAddible resultstorage, 
            NGSAddible failedstorage, Run run, int threadnumber
    ) throws NoDataBaseRespondException, SQLException {
        this.ngsfile = source;
        this.manager = new BLASTManager(resultstorage, failedstorage, threadnumber);
        this.indatabase = DBService.INSTANCE.getStoragedSequences(run);
    }

    synchronized public void startBLAST() {
        final Runnable sender = new Runnable() {
            @Override
            public void run() {
                NGSRecord record;
                while ((record = ngsfile.getNGSRecord()) != null) {
                    if (!indatabase.contains(record.recordid)) {
                        manager.processRecord(record);
                    }
                }
                manager.shutdown();
            }
        };
        (new Thread(sender)).start();
    }
    
}
