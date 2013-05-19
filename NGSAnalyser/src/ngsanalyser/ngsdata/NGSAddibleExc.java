package ngsanalyser.ngsdata;

import java.util.Collection;

public interface NGSAddibleExc {
    public void addNGSRecord(NGSRecord record, Exception ex);
    public void addNGSRecordsCollection(Collection<NGSRecord> records, Exception ex);
    public void terminate();
}
