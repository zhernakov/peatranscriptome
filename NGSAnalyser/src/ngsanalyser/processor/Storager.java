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

    public Storager(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber, Run run) {
        super(resultstorage, failedstorage, threadnumber);
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
            final Storaging thread = new Storaging(this, sublist, run);
            startNewThread(thread);
        }
        if (terminated) {
            final List<NGSRecord> sublist = new LinkedList<>();
            while (!storage.isEmpty()) {
                sublist.add(storage.remove(0));
            }
            count = 0;
        }
    }

    void insertCompleted(Storaging thread) {
        recordsProcessed(thread.getRecords());
        eliminateThread(thread);
    }

    void insertFailed(Storaging thread, Exception ex) {
        recordsProcessingFailed(thread.getRecords());
        eliminateThread(thread);
    }

    @Override
    public synchronized void terminate() {
        flushStorage(true);
        super.terminate();
    }
}
