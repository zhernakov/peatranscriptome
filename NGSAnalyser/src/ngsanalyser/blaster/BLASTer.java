package ngsanalyser.blaster;

import ngsanalyser.ngsdata.NGSRecordsCollection;
import ngsanalyser.ngsdata.NGSFile;
import ngsanalyser.ngsdata.NGSRecord;

public class BLASTer {
    private final NGSFile ngsfile;
    private final BLASTManager manager;

    public BLASTer(NGSFile sourcefile, NGSRecordsCollection resultstorage, int threadnumber) {
        this.ngsfile = sourcefile;
        this.manager = new BLASTManager(threadnumber, resultstorage);
    }

    synchronized public void startBLAST() {
        final Runnable sender = new Runnable() {
            @Override
            public void run() {
                NGSRecord record;
                while ((record = ngsfile.next()) != null) {
                    manager.startNewBLAST(record);
                }
                manager.shutdown();
            }
        };
        (new Thread(sender)).start();
    }
    
}
