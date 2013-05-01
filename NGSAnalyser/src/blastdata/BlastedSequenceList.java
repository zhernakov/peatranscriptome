package blastdata;

import java.util.LinkedList;
import java.util.List;
import org.biojava3.sequencing.io.fastq.Fastq;

public class BlastedSequenceList {
    private final List<Fastq> records = new LinkedList<>();
    private final List<String> results = new LinkedList<>();
    
    synchronized public void setBlastResult(Fastq record) {
        records.add(record);
    }
}
