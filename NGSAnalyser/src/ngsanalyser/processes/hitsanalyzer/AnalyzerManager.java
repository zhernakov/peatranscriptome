package ngsanalyser.processes.hitsanalyzer;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessManager;

public class AnalyzerManager extends ProcessManager {
    public AnalyzerManager(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        super(resultstorage, failedstorage, threadnumber);
    }

    @Override
    synchronized public void processRecord(NGSRecord record) {
        final AnalyzingThread process = new AnalyzingThread(this, record, 1e-25);
        newRecordProcessing(process);
    }

    @Override
    synchronized public void recordProcessed(NGSRecord record) {
        if (record.getTaxonId() == -1) {
            if (record.isConnectionLost()) {
                record.resetConnectionFlag();
                restartRecordProcessing(new AnalyzingThread(this, record, 1e-25));
            } else {
                recordCanNotBeProcessed(record);
            }
        } else {
            recordSuccessfullyProcesed(record);
        }
    }
    
}
