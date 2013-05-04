package ngsanalyser.blaster;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessManager;

public class BLASTManager extends ProcessManager {
    private final int timeinterval = 250;

    public BLASTManager(int threadnumber, NGSAddible resultstorage) {
        super(threadnumber, resultstorage);
        setTimer(timeinterval);
    }

    @Override
    synchronized public void processRecord(NGSRecord record) {
        final BLASTQuery query = new BLASTQuery(this, record);
        newRecordProcessing(query);
    }

    @Override
    synchronized public void recordProcessed(NGSRecord record) {
        if (record.getBlastResultFilePath() == null) {
            if (record.isConnectionLost()) {
                record.resetConnectionFlag();
                final BLASTQuery query = new BLASTQuery(this, record);
                restartRecordProcessing(query);
            } else {
                recordCanNotBeProcessed(record);
            }
        } else {
            recordSuccessfullyProcesed(record);
        }
    }
}
