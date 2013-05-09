package ngsanalyser.processes.databasestorager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.experiment.Experiment;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

class StoragerManager {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private int threadnumber;
    private int threadinwork = 0;
    private final Run run;
    private final NGSAddible failedstorage;

    StoragerManager(NGSAddible failedstorage, Run run, int threadnumber) {
        this.run = run;
        this.threadnumber = threadnumber;
        this.failedstorage = failedstorage;
    }

    synchronized void storageRecords(List<NGSRecord> records) {
        try {
            while (threadinwork >= threadnumber) {
                wait();
            }
            executor.execute(new StorageThread(this, run, records));
        } catch (InterruptedException ex) {
            Logger.getLogger(StoragerManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    synchronized void terminate() {
        executor.shutdown();
    }

    synchronized void insertCompleted() {
        --threadinwork;
        notify();
    }

    synchronized void insertFailed(List<NGSRecord> storage, Exception ex) {
        failedstorage.addNGSRecordsCollection(storage);
        --threadinwork;
        notify();
    }
    
   
}
