package ngsanalyser.processes.blaster;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessManager;

public class BLASTManager extends ProcessManager {
    private final int timeinterval = 250;

    public BLASTManager(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        super(resultstorage, failedstorage, threadnumber);
        setTimer(timeinterval);
    }

    @Override
    synchronized public void processRecord(NGSRecord record) {
        final BLASTQuery query = new BLASTQuery(this, record);
        newRecordProcessing(query);
    }

    @Override
    synchronized public void recordProcessed(NGSRecord record) {
        if (record.getBLASTHits() != null) {
            recordSuccessfullyProcesed(record);
        } else {
            if (record.isConnectionLost()) {
                record.resetConnectionFlag();
                final BLASTQuery query = new BLASTQuery(this, record);
                restartRecordProcessing(query);
            } else {
                recordCanNotBeProcessed(record);
            }
        }
    }
}
