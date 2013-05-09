package ngsanalyser.processes.blaster;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessesManager;

public class BLASTManager extends ProcessesManager {
    public BLASTManager(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        super(resultstorage, failedstorage, threadnumber);
    }

    @Override
    synchronized public void processRecord(NGSRecord record) {
        final BLASTQuery query = new BLASTQuery(this, record);
        startNewThread(query);
    }

    @Override
    synchronized public void recordProcessed(NGSRecord record) {
        if (record.getBLASTHits() != null) {
            recordSuccessfullyProcesed(record);
        } else {
            if (record.isConnectionLost()) {
                record.resetConnectionFlag();
                final BLASTQuery query = new BLASTQuery(this, record);
                restartThread(query);
            } else {
                recordCanNotBeProcessed(record);
            }
        }
    }
}
