package ngsanalyser.xmlparser;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSCollectable;
import ngsanalyser.ngsdata.NGSRecord;

public class XMLParser {
    private final NGSCollectable source;
    private final XMLParserManager manager;

    public XMLParser(NGSCollectable source, NGSAddible target) {
        this.source = source;
        manager = new XMLParserManager(2, target);
    }
    
    public void startParsing() {
        final Runnable sender = new Runnable() {
            @Override
            public void run() {
                NGSRecord record;
                while ((record = source.getNGSRecord()) != null) {
                    manager.startParsing(record);
                }
                manager.shutdown();
            }
        };
        (new Thread(sender)).start();
    }
}
