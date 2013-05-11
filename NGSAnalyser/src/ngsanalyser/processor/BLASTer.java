package ngsanalyser.processor;

import java.util.Collection;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class BLASTer extends AbstractProcessor {
    
    public BLASTer(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        super(resultstorage, failedstorage, threadnumber);
    }

    @Override
    public void addNGSRecord(NGSRecord record) {
        final BLASTQuery query = new BLASTQuery(this, record);
        startNewThread(query);
    }

    @Override
    public void addNGSRecordsCollection(Collection<NGSRecord> records) {
        for (final NGSRecord record : records) {
            addNGSRecord(record);
        }
    }

    void threadSuccessfullyFinished(BLASTQuery query) {
        recordProcessed(query.getRecord());
        eliminateThread(query);
    }

    void threadProcessingFailed(BLASTQuery query, Exception ex) {
        if (ex instanceof NoConnectionException) {
            restartThread(query);
        } else {
            recordProcessingFailed(query.getRecord());
            eliminateThread(query);
        }
    }
}
