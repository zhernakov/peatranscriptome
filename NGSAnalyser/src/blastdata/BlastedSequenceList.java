package blastdata;

import java.util.LinkedList;
import java.util.List;
import ngsanalyser.ngsdata.NGSRecord;

public class BlastedSequenceList {
    private final List<NGSRecord> records = new LinkedList<>();
    
    synchronized public void setBlastResult(NGSRecord record) {
        records.add(record);
    }
}
