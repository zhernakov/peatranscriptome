package ngsanalyser.processor;

import java.sql.SQLException;
import java.util.Collection;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.NoDataBaseResponseException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class Storager extends AbstractMultiProcessor {
    private final Run run;

    public Storager(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber, int bunchsize, Run run) {
        super("Storager", resultstorage, failedstorage, threadnumber, bunchsize);
        this.run = run;
    }

    @Override
    protected Process createProcess(Collection<NGSRecord> bunch) {
        return new Storaging(bunch);
    }
    
    /////////////
    
    private class Storaging extends Process {
        private final Collection<NGSRecord> records;

        public Storaging(Collection<NGSRecord> records) {
            this.records = records;
        }

        @Override
        protected void processing() throws SQLException, NoDataBaseResponseException {
            DBService.INSTANCE.addSequences(run, records);
        }

        @Override
        protected Process cloneProcess() {
            return new Storaging(records);
        }

        @Override
        protected Collection<NGSRecord> getRecords() {
            return records;
        }
    }
}
