package ngsanalyser.ngsdata;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NGSRecordsCollection implements NGSCollectable, NGSAddible{
    private final List<NGSRecord> list = new LinkedList<>();
    private boolean terminated = false;
    
    @Override
    synchronized public void terminate() {
        terminated = true;
        notify();
    }
    
    @Override
    synchronized public void addNGSRecord(NGSRecord record) {
        if (!terminated) {
            list.add(record);
            notify();
        }
    }

    @Override
    synchronized public void addNGSRecordsCollection(Collection<NGSRecord> records) {
        if (!terminated) {
            list.addAll(records);
            notify();
        }
    }
    
    @Override
    synchronized public NGSRecord getNGSRecord() {
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

    public int getNumber() {
        return list.size();
    }
}
