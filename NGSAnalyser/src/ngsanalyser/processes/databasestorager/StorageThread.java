package ngsanalyser.processes.databasestorager;

import java.sql.SQLException;
import java.util.List;
import ngsanalyser.Experiment;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.ngsdata.NGSRecord;

class StorageThread implements Runnable {
    private final StoragerManager manager;
    private final Experiment experiment;
    private final List<NGSRecord> records;

    StorageThread(StoragerManager manager, Experiment experiment, List<NGSRecord> storage) {
        this.manager = manager;
        this.experiment = experiment;
        this.records = storage;
    }

    @Override
    public void run() {
        try {
            final StringBuilder query = experiment.composeSequencesInsertQuery(records);
            DBService.INSTANCE.sendInsertQuery(query);
            manager.insertCompleted();
        } catch (SQLException ex) {
            manager.insertFailed(records, ex);
        } catch (NoConnectionException ex) {
            manager.insertFailed(records, ex);
        }
    }
    
}
