package ngsanalyser.processor;

import java.util.Map;
import java.util.TreeMap;

public class StringTree {
    private final Map<String,String> tree = new TreeMap<>();
    
    public boolean isRecordStored(String id) {
        return tree.containsKey(id);
    }

    public void addString(String string) {
        tree.put(string, null);
    }
}
