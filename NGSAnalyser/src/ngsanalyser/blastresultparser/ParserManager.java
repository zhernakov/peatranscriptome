package ngsanalyser.blastresultparser;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessManager;

public class ParserManager extends ProcessManager {
    public ParserManager(int threadnumber, NGSAddible resultstorage) {
        super(threadnumber, resultstorage);
    }
    
    @Override
    synchronized public void processRecord(NGSRecord record) {
        final ParsingThread process = new ParsingThread(this, record);
        newRecordProcessing(process);
    }
    
    @Override
    synchronized public void recordProcessed(NGSRecord record) {
        if (record.getBLASTHits() == null) {
            recordCanNotBeProcessed(record);
        } else {
            recordSuccessfullyProcesed(record);
        }
    }
}
