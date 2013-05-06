package taxonomy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TaxonRS {

    private final Map<Integer, List<Integer>> db = new HashMap<>();

    void add(Integer taxid, Integer parentid) {
        if (!db.containsKey(parentid)) {
            db.put(parentid, new LinkedList<Integer>());
        }
        List<Integer> list = db.get(parentid);
        list.add(taxid);
    }

    void write(File out) throws FileNotFoundException, IOException {
        final FileOutputStream stream = new FileOutputStream(out);

        final List<Integer> parentslist = new LinkedList<>();
        parentslist.addAll(db.get(new Integer(1)));
        parentslist.remove(new Integer(1));
        stream.write(code(1,1));
        
        while (!parentslist.isEmpty()) {
            final Integer parent = parentslist.remove(0);
            final List<Integer> taxids = db.get(parent);
            if (taxids != null) {
                parentslist.addAll(taxids);
                for (final Integer taxid : taxids) {
                    stream.write(code(taxid, parent));
                }
            }
        }
        
        stream.close();
    }

    private static byte[] code(int taxid, int parent) {
        byte[] array = new byte[]{
            (byte) (taxid >>> 16),
            (byte) (taxid >>> 8),
            (byte) (taxid),
            (byte) (parent >>> 16),
            (byte) (parent >>> 8),
            (byte) (parent),};
        return array;
    }
}
