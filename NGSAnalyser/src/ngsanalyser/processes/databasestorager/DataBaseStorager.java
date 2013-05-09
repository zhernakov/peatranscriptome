package ngsanalyser.processes.databasestorager;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import ngsanalyser.experiment.Experiment;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class DataBaseStorager implements NGSAddible {
    private List<NGSRecord> storage = new LinkedList<>();
    private boolean terminated = false;
    private int querysize = 10;
    private int count = 0;

    private final StoragerManager manager;

    public DataBaseStorager(NGSAddible failedstorage, Run run) {
        this.manager = new StoragerManager(failedstorage, run, 2);
    }
    
    @Override
    synchronized public void addNGSRecord(NGSRecord record) {
        if (!terminated) {
            storage.add(record);
            if (++count == querysize) {
                flushStorage();
            }
        }
    }

    @Override
    synchronized public void addNGSRecordsCollection(Collection<NGSRecord> records) {
        if (!terminated) {
            storage.addAll(records);
            if ((count += records.size()) >= querysize) {
                flushStorage();
            }
        }
    }

    @Override
    synchronized public void terminate() {
        terminated = true;
        flushStorage();
        manager.terminate();
    }

    @Override
    public int getNumber() {
        return storage.size();
    }

    private void flushStorage() {
        while (storage.size() >= querysize) {
            final List<NGSRecord> sublist = new LinkedList<>();
            for (int i = 0; i < querysize; ++i) {
                sublist.add(storage.remove(0));
            }
            manager.storageRecords(sublist);
            count -= querysize;
        }
        if (terminated) {
            final List<NGSRecord> sublist = new LinkedList<>();
            while (!storage.isEmpty()) {
                sublist.add(storage.remove(0));
            }
            manager.storageRecords(sublist);
            count = 0;
        }
    }

    
}
