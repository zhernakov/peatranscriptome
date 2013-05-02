package ngsanalyser.blastparser;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSCollectable;

public class Parser {
    private final NGSCollectable source;
    private final NGSAddible storage;

    public Parser(NGSCollectable source, NGSAddible target) {
        this.source = source;
        this.storage = target;
    }
    
    public void startParsing() {
        
    }
}
