package ngsanalyser.processor;

import java.util.Collection;
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
        final HitsScan scan = new HitsScan(this, resultstorage, failedstorage, evalue, record);
        startNewThread(scan);
    }

    @Override
    public void addNGSRecordsCollection(Collection<NGSRecord> records) {
        for (final NGSRecord record : records) {
            addNGSRecord(record);
        }
    }
}
