package ngsanalyser.processes.hitsanalyzer;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSCollectable;
import ngsanalyser.ngsdata.NGSRecord;

public class HitsAnalyzer {
    private final NGSCollectable source;
    private final AnalyzerManager manager;

    public HitsAnalyzer(
            NGSCollectable source,  NGSAddible resultstorage, 
            NGSAddible failedstorage, int threadnumber
    ) {
        this.source = source;
        manager = new AnalyzerManager(resultstorage, failedstorage, threadnumber);
    }
    
    public void startAnalysis () {
        final Runnable sender = new Runnable() {
            @Override
            public void run() {
                NGSRecord record;
                while ((record = source.getNGSRecord()) != null) {
                    manager.processRecord(record);
                }
                manager.shutdown();
            }
        };
        (new Thread(sender)).start();
    }
}
