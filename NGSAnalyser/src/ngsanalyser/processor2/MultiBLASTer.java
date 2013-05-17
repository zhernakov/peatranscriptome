package ngsanalyser.processor2;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import ngsanalyser.exception.BLASTException;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ncbiservice.NCBIParser;
import ngsanalyser.ncbiservice.NCBIService2;
import ngsanalyser.ncbiservice.blast.BlastHits;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class MultiBLASTer extends AbstractMultiProcessor {

    public MultiBLASTer(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber, int bunchsize) {
        super("BLASTer", resultstorage, failedstorage, threadnumber, bunchsize);
    }

    @Override
    protected Process createProcess(Collection<NGSRecord> bunch) {
        return new BLUSTQuery(bunch);
    }
    
    /////////////
    
    private final class BLUSTQuery extends Process {
        private final Collection<NGSRecord> records;

        private BLUSTQuery(Collection<NGSRecord> records) {
            this.records = records;
        }

        @Override
        protected void processing() throws NoConnectionException, BLASTException, ParseException {
            final InputStream stream = NCBIService2.INSTANCE.multiMegaBlast(records);
            final Map<String,BlastHits> hits = NCBIParser.parseBlastResult(stream);
            for (final NGSRecord record : records) {
                record.setBLASTHits(hits.get(record.recordid));
            }
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
