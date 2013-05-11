package ngsanalyser.processor;

import java.util.Collection;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class HitsAnalyzer extends AbstractProcessor {
    private static double evalue = 1e-25;

    public static void setEvalue(double evalue) {
        HitsAnalyzer.evalue = evalue;
    }
    
    public HitsAnalyzer(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        super(resultstorage, failedstorage, threadnumber);
    }

    @Override
    public void addNGSRecord(NGSRecord record) {
        final HitsScan scan = new HitsScan(this, record, evalue);
        startNewThread(scan);
    }

    @Override
    public void addNGSRecordsCollection(Collection<NGSRecord> records) {
        for (final NGSRecord record : records) {
            addNGSRecord(record);
        }
    }

    void threadSuccessfullyFinished(HitsScan scan) {
        recordProcessed(scan.getRecord());
        eliminateThread(scan);
    }

    void threadProcessingFailed(HitsScan scan, Exception ex) {
        if (ex instanceof NoConnectionException) {
            restartThread(scan);
        } else {
            recordProcessingFailed(scan.getRecord());
            eliminateThread(scan);
        }
    }
}
