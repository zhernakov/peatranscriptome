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
    synchronized public void startProcess(NGSRecord record) {
        try {
            while (threadinwork >= threadnumber)
                wait();
            startBLAST(record);
        } catch (InterruptedException ex) {
            
        }
    }

    private void startBLAST(NGSRecord record) {
        final BLASTQuery query = new BLASTQuery(this, record);
        executor.execute(query);
        ++threadinwork;
        try {
            Thread.sleep(timeinterval);
        } catch (InterruptedException ex) {
            Logger.getLogger(BLASTManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    synchronized public void finishProcess(NGSRecord record) {
        if (record.getBlastResult() == null) {
            startBLAST(record);
        } else {
            ProcessSuccessful(record);
        }
    }
}
