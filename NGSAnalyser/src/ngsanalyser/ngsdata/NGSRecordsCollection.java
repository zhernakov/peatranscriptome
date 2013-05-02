package ngsanalyser.ngsdata;

import java.io.PipedWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NGSRecordsCollection {
    private final List<NGSRecord> list = new LinkedList<>();
    private boolean terminated = false;
    
    synchronized public void terminate() {
        terminated = true;
    }
    
    synchronized public void addNGSRecord(NGSRecord record) {
        if (!terminated) {
            list.add(record);
            notify();
        }
    }
    
    synchronized NGSRecord getNGSRecord() {
        while (!terminated && list.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(NGSRecordsCollection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (terminated && list.isEmpty()) {
            return null;
        } else {
            return list.remove(0);
        }
    }
}
