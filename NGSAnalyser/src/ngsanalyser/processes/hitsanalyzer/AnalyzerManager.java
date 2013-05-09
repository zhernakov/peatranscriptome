package ngsanalyser.processes.hitsanalyzer;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessesManager;

public class AnalyzerManager extends ProcessesManager {
    public AnalyzerManager(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        super(resultstorage, failedstorage, threadnumber);
    }

    @Override
    synchronized public void processRecord(NGSRecord record) {
        final AnalyzingThread process = new AnalyzingThread(this, record, 1e-25);
        startNewThread(process);
    }

    @Override
    synchronized public void recordProcessed(NGSRecord record) {
        if (record.getTaxonId() == -1) {
            if (record.isConnectionLost()) {
                record.resetConnectionFlag();
                restartThread(new AnalyzingThread(this, record, 1e-25));
            } else {
                recordCanNotBeProcessed(record);
            }
        } else {
            recordSuccessfullyProcesed(record);
        }
    }
    
}
