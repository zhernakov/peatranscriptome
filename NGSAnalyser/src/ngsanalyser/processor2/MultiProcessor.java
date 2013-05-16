package ngsanalyser.processor2;

import java.util.LinkedList;
import java.util.List;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public abstract class MultiProcessor extends AbstractProcessor {
    private int bunchsize;
    private int inbunch = 0;
    private List<NGSRecord> bunchstorage = new LinkedList<>();

    public MultiProcessor(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber, int bunchsize) {
        super(resultstorage, failedstorage, threadnumber);
        this.bunchsize = bunchsize;
    }

    @Override
    public final void addNGSRecord(NGSRecord record) {
        bunchstorage.add(record);
        if (++inbunch == bunchsize) {
            startNewProcess(createProcess(bunchstorage));
            bunchstorage = new LinkedList<>();
            inbunch = 0;
        }
    }

    @Override
    public final void terminate() {
        startNewProcess(createProcess(bunchstorage));
        bunchstorage = null;
        inbunch = -1;
        super.terminate();
    }

    protected abstract Process createProcess(List<NGSRecord> bunch);
}
