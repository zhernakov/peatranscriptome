package ngsanalyser.processor;

import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSAddibleExc;
import ngsanalyser.ngsdata.NGSRecord;

public abstract class AbstractSingleProcessor extends AbstractProcessor {

    public AbstractSingleProcessor(String name, NGSAddible resultstorage, NGSAddibleExc failedstorage, int threadnumber) {
        super(name, resultstorage, failedstorage, threadnumber);
    }
    
    @Override
    public synchronized final void addNGSRecord(NGSRecord record) {
        startNewProcess(createProcess(record));
    }

    protected abstract Process createProcess(NGSRecord record);
}
