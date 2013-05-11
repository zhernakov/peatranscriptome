package ngsanalyser.processor;

import java.util.Collection;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class BLASTer extends AbstractProcessor {
    
    public BLASTer(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        super(resultstorage, failedstorage, threadnumber);
    }

    @Override
    public void addNGSRecord(NGSRecord record) {
        final BLASTQuery query = new BLASTQuery(this, resultstorage, failedstorage, record);
        startNewThread(query);
    }

    @Override
    public void addNGSRecordsCollection(Collection<NGSRecord> records) {
        for (final NGSRecord record : records) {
            addNGSRecord(record);
        }
    }
}
