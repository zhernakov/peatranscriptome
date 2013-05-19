package ngsanalyser.processor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public abstract class AbstractMultiProcessor extends AbstractProcessor {
    private int bunchsize;
    private int inbunch = 0;
    private List<NGSRecord> bunchstorage = new LinkedList<>();

    public AbstractMultiProcessor(String name, NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber, int bunchsize) {
        super(name, resultstorage, failedstorage, threadnumber);
        this.bunchsize = bunchsize;
    }

    @Override
    public synchronized final void addNGSRecord(NGSRecord record) {
        bunchstorage.add(record);
        if (++inbunch == bunchsize) {
            startNewProcess(createProcess(bunchstorage));
            bunchstorage = new LinkedList<>();
            inbunch = 0;
        }
    }

    @Override
    public synchronized final void terminate() {
        if (inbunch > 0) {
            startNewProcess(createProcess(bunchstorage));
        }
        bunchstorage = null;
        inbunch = -1;
        super.terminate();
    }

    protected abstract Process createProcess(Collection<NGSRecord> bunch);
}
