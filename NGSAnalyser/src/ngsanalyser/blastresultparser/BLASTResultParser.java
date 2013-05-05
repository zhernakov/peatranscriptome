package ngsanalyser.blastresultparser;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSCollectable;
import ngsanalyser.ngsdata.NGSRecord;

public class BLASTResultParser {
    private final NGSCollectable source;
    private final ParserManager manager;

    public BLASTResultParser(NGSCollectable source, NGSAddible resultstorage, NGSAddible failedstorage) {
        this.source = source;
        manager = new ParserManager(resultstorage, failedstorage, 2);
    }
    
    public void startParsing() {
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
