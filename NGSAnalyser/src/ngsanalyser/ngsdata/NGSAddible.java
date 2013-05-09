package ngsanalyser.ngsdata;

import java.util.Collection;

public interface NGSAddible {
    public void addNGSRecord(NGSRecord record);
    public void addNGSRecordsCollection(Collection<NGSRecord> records);
    public void terminate();
    public int getNumber();

}
