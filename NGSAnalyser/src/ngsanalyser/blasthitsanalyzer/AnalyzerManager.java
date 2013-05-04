package ngsanalyser.blasthitsanalyzer;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessManager;
import ngsanalyser.taxonomy.Taxonomy;

public class AnalyzerManager extends ProcessManager {
    private final Taxonomy taxonomy;
    
    public AnalyzerManager(int threadnumber, NGSAddible resultstorage, Taxonomy taxonomy) {
        super(threadnumber, resultstorage);
        this.taxonomy = taxonomy;
    }

    @Override
    synchronized public void processRecord(NGSRecord record) {
        final AnalyzingThread process = new AnalyzingThread(this, record, taxonomy, 1e-25);
        newRecordProcessing(process);
    }

    @Override
    synchronized public void recordProcessed(NGSRecord record) {
        if (record.getTaxonId() == -1) {
            if (record.isConnectionLost()) {
                record.resetConnectionFlag();
                restartRecordProcessing(new AnalyzingThread(this, record, taxonomy, 1e-25));
            } else {
                recordCanNotBeProcessed(record);
            }
        } else {
            recordSuccessfullyProcesed(record);
        }
    }
    
}
