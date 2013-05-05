package ngsanalyser.processes.blaster;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSCollectable;
import ngsanalyser.ngsdata.NGSRecord;

public class BLASTer {
    private final NGSCollectable ngsfile;
    private final BLASTManager manager;

    public BLASTer(NGSCollectable source, NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        this.ngsfile = source;
        this.manager = new BLASTManager(resultstorage, failedstorage, threadnumber);
    }

    synchronized public void startBLAST() {
        final Runnable sender = new Runnable() {
            @Override
            public void run() {
                NGSRecord record;
                while ((record = ngsfile.getNGSRecord()) != null) {
                    manager.processRecord(record);
                }
                manager.shutdown();
            }
        };
        (new Thread(sender)).start();
    }
    
}
