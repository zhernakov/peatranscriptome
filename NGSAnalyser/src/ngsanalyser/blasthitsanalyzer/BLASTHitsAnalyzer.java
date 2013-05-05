package ngsanalyser.blasthitsanalyzer;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSCollectable;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.taxonomy.Taxonomy;

public class BLASTHitsAnalyzer {
    private final NGSCollectable source;
    private final AnalyzerManager manager;

    public BLASTHitsAnalyzer(
            NGSCollectable source,  NGSAddible resultstorage, 
            NGSAddible failedstorage, int threadnumber, Taxonomy taxonomy
    ) {
        this.source = source;
        manager = new AnalyzerManager(resultstorage, failedstorage, threadnumber, taxonomy);
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
