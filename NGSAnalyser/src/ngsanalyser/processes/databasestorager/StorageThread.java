package ngsanalyser.processes.databasestorager;

import java.sql.SQLException;
import java.util.List;
import ngsanalyser.experiment.Experiment;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSRecord;

class StorageThread implements Runnable {
    private final StoragerManager manager;
    private final Run run;
    private final List<NGSRecord> records;

    StorageThread(StoragerManager manager, Run run, List<NGSRecord> storage) {
        this.manager = manager;
        this.run = run;
        this.records = storage;
    }

    @Override
    public void run() {
        try {
            DBService.INSTANCE.addSequences(run, records);
            manager.insertCompleted();
        } catch (SQLException ex) {
            manager.insertFailed(records, ex);
            System.err.println(ex);
        }
    }
    
}
