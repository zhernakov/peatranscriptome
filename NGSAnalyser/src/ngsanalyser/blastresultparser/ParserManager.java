package ngsanalyser.blastresultparser;

import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.processes.ProcessManager;

public class ParserManager extends ProcessManager {
    public ParserManager(int threadnumber, NGSAddible resultstorage) {
        super(threadnumber, resultstorage);
    }
    
    @Override
    synchronized public void startProcess(NGSRecord record) {
        try {
            while (threadinwork >= threadnumber) {
                wait();
            }
            final ParsingThread process = new ParsingThread(this, record);
            executor.execute(process);
            ++threadinwork;
        } catch (InterruptedException ex) {
            Logger.getLogger(ParserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    synchronized public void finishProcess(NGSRecord record) {
        ProcessSuccessful(record);
    }
}
