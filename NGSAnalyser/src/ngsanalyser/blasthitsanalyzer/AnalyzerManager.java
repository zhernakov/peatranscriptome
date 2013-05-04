package ngsanalyser.blasthitsanalyzer;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessManager;

public class AnalyzerManager extends ProcessManager {

    public AnalyzerManager(int threadnumber, NGSAddible resultstorage) {
        super(threadnumber, resultstorage);
    }

    @Override
    synchronized public void processRecord(NGSRecord record) {
        final AnalyzingThread process = new AnalyzingThread(this, record, 1e-25);
        processIsReady(process);
    }

    @Override
    synchronized public void recordProcessed(NGSRecord record) {
        processSuccessfullyFinished(record);
    }
    
}
