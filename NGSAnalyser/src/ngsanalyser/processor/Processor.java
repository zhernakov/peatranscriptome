package ngsanalyser.processor;

import ngsanalyser.ngsdata.NGSFile;

public class Processor {
    private final int blastintrval = 250;
    private final NGSFile ngsfile;
    
    public Processor(NGSFile ngsfile) {
        this.ngsfile = ngsfile;
    }
    
    public void startAnalysis() throws InterruptedException {
    }
}
