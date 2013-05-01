package ngsanalyser.blaster;

import blastdata.BlastedSequenceList;
import ngsanalyser.ngsdata.NGSFile;

public class BLASTer {
    private final NGSFile ngsfile;
    private final BLASTManager manager;

    public BLASTer(NGSFile sourcefile, BlastedSequenceList resultstorage, int threadnumber) {
        this.ngsfile = sourcefile;
        this.manager = new BLASTManager(threadnumber, resultstorage);
    }

    synchronized public void startBLAST() {
        final Runnable sender = new Runnable() {
            @Override
            public void run() {
                while (ngsfile.hasNext()) {
                    manager.startNewBLAST(ngsfile.next());
                }
                manager.shutdown();
            }
        };
        (new Thread(sender)).start();
    }
    
}
