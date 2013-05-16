package ngsanalyser.processor2;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ncbiservice.NCBIService;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class MultiBLUSTer extends MultiProcessor {

    public MultiBLUSTer(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber, int bunchsize) {
        super(resultstorage, failedstorage, threadnumber, bunchsize);
    }

    @Override
    protected Process createProcess(List<NGSRecord> bunch) {
        return new BLUSTQuery(bunch);
    }
    
/////////////
    
    private final class BLUSTQuery extends Process {
        private final List<NGSRecord> records;

        private BLUSTQuery(List<NGSRecord> records) {
            this.records = records;
        }

        @Override
        protected void processing() throws NoConnectionException, ParseException {
            NCBIService.INSTANCE.multiblast(records);
        }

        @Override
        protected Process cloneProcess() {
            return new BLUSTQuery(records);
        }

        @Override
        protected Collection<NGSRecord> getRecords() {
            return records;
        }
        
    }
}
