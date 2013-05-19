package ngsanalyser.processor;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import ngsanalyser.exception.BLASTException;
import ngsanalyser.exception.NCBIConnectionException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ncbiservice.NCBIParser;
import ngsanalyser.ncbiservice.NCBIService;
import ngsanalyser.ncbiservice.blast.BlastHits;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSAddibleExc;
import ngsanalyser.ngsdata.NGSRecord;

public class MultiBLASTer extends AbstractMultiProcessor {

    public MultiBLASTer(NGSAddible resultstorage, NGSAddibleExc failedstorage, int threadnumber, int bunchsize) {
        super("MultiBLASTer", resultstorage, failedstorage, threadnumber, bunchsize);
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
        protected void processing() throws NCBIConnectionException, BLASTException, ParseException {
            final InputStream stream = NCBIService.INSTANCE.multiMegaBlast(records);
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
