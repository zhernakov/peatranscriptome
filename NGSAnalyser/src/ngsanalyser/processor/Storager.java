package ngsanalyser.processor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class Storager extends AbstractProcessor {
    private final Run run;
    private List<NGSRecord> storage = new LinkedList<>();
    private int querysize = 10;
    private int count = 0;

    public Storager(NGSAddible failedstorage, int threadnumber, Run run) {
        super(null, failedstorage, threadnumber);
        this.run = run;
    }

    @Override
    public synchronized void addNGSRecord(NGSRecord record) {
        storage.add(record);
        if (++count == querysize) {
            flushStorage(false);
        }
    }

    @Override
    public synchronized void addNGSRecordsCollection(Collection<NGSRecord> records) {
        storage.addAll(records);
        if ((count += records.size()) >= querysize) {
            flushStorage(false);
        }
    }

    private void flushStorage(boolean terminated) {
        while (storage.size() >= querysize) {
            final List<NGSRecord> sublist = new LinkedList<>();
            for (int i = 0; i < querysize; ++i) {
                sublist.add(storage.remove(0));
            }
            count -= querysize;
            startStorageProcedure(sublist);
        }
        if (terminated) {
            final List<NGSRecord> sublist = new LinkedList<>();
            while (!storage.isEmpty()) {
                sublist.add(storage.remove(0));
            }
            count = 0;
            if (!sublist.isEmpty()) {
                startStorageProcedure(sublist);
            }
        }
    }
    
    private void startStorageProcedure(List<NGSRecord> records) {
        final Storaging storaging = new Storaging(this, failedstorage, run, records);
        startNewThread(storaging);
    }

    @Override
    public synchronized void terminate() {
        flushStorage(true);
        super.terminate();
    }
}
