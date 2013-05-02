package ngsanalyser.blastparser;

import ngsanalyser.ngsdata.NGSRecordsCollection;

public class Parser {
    private final NGSRecordsCollection source;
    private final NGSRecordsCollection target;

    public Parser(NGSRecordsCollection source, NGSRecordsCollection target) {
        this.source = source;
        this.target = target;
    }
    
    public void startParsing() {
        
    }
}
