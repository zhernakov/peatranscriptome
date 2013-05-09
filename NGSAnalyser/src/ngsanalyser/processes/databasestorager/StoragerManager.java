package ngsanalyser.processes.databasestorager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.Experiment;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

class StoragerManager {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private int threadnumber;
    private int threadinwork = 0;
    private final Experiment experiment;
    private final NGSAddible failedstorage;

    StoragerManager(NGSAddible failedstorage, Experiment experiment, int threadnumber) {
        this.experiment = experiment;
        this.threadnumber = threadnumber;
        this.failedstorage = failedstorage;
    }

    synchronized void storageRecords(List<NGSRecord> records) {
        try {
            while (threadinwork >= threadnumber) {
                wait();
            }
            executor.execute(new StorageThread(this, experiment, records));
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
