package ngsanalyser.blaster;

import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessManager;

public class BLASTManager extends ProcessManager {
    private final int timeinterval = 250;

    public BLASTManager(int threadnumber, NGSAddible resultstorage) {
        super(threadnumber, resultstorage);
    }

    @Override
    synchronized public void processRecord(NGSRecord record) {
        final BLASTQuery query = new BLASTQuery(this, record);
        processIsReady(query);
        try {
            Thread.sleep(timeinterval);
        } catch (InterruptedException ex) {
            Logger.getLogger(BLASTManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    synchronized public void recordProcessed(NGSRecord record) {
        if (record.getBlastResultFilePath() == null) {
            final BLASTQuery query = new BLASTQuery(this, record);
            restartProcess(query);
        } else {
            processSuccessfullyFinished(record);
        }
    }
}
