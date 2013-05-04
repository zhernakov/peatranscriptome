package ngsanalyser.taxonomy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Taxonomy {
    private static Taxonomy taxonomy;
    private static String defaultpath = "D:\\_workspace\\peatranscriptome\\_data\\taxonomy";
    
    synchronized public static Taxonomy getDefaultInstance() throws TaxonomyException {
        if (taxonomy == null) {
            taxonomy = new Taxonomy(defaultpath);
        }
        return taxonomy;
    }
    
    synchronized public static Taxonomy getNewInstance(String path) throws TaxonomyException {
        return new Taxonomy(path);
    }

    synchronized public static void setDefaultpath(String defaultpath) {
        Taxonomy.defaultpath = defaultpath;
    }
    
    private final Map<Integer,Integer> db = new TreeMap<>();

    private Taxonomy(String path) throws TaxonomyException {
        FileInputStream in = null;
        final byte[] array = new byte[6];

        try {
            in = new FileInputStream(new File(path));
            while (in.read(array) != -1) {
                int taxid = ((((int)array[0]) & 0xFF) << 16) | ((((int)array[1]) & 0xFF) << 8) | (((int)array[2]) & 0xFF);
                int parent = ((((int)array[3]) & 0xFF) << 16) | ((((int)array[4]) & 0xFF) << 8) | (((int)array[5]) & 0xFF);
                Integer put = db.put(taxid, parent);
                if (put != null) {
                    System.err.println(taxid + " " + parent + " " + put);
                }
            }
        } catch (FileNotFoundException e) {
            throw new TaxonomySrcFileException("Can't open taxons hierarchy file " + path);
        } catch (IOException e) {
            throw new TaxonomySrcFileException("IO problem while reading taxons hierarchy file " + path);
        } catch (Exception e) {
            Logger.getLogger(Taxonomy.class.getName()).log(Level.SEVERE, null, e);
            throw new TaxonomySrcFileException("Logical problem while reading taxons hierarchy file " + path +
                    " propably file structure is corupted");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                
            }
        }
    }

    public int getTaxonNumber() {
        return db.size();
    }
    
    public boolean hasTaxon(Integer taxid) {
        return db.containsKey(taxid);
    }
    
    public int findCommonAncestor(Collection<Integer> list) throws TaxonomyHierarchyException{
        final Iterator<Integer> it = list.iterator();
        final LinkedList<Integer> ancestors = defineAncestors(it.next());
        while (it.hasNext()) {
            final Integer member = it.next();
            cutAncestorsPath(ancestors, member);
        }
        return ancestors.get(0);
    };

    private LinkedList<Integer> defineAncestors(Integer taxid) throws TaxonomyHierarchyException {
        final LinkedList<Integer> ancestors = new LinkedList<>();
        ancestors.add(taxid);
        Integer parentid;
        while ((parentid = db.get(taxid)) != taxid) {
            if (parentid == null) {
                throw new TaxonomyHierarchyException("Can't find parent taxon to taxon with id " + taxid);
            }
            taxid = parentid;
            ancestors.add(taxid);
            if (ancestors.size() > 500) {
                throw new TaxonomyHierarchyException("more than 500 steps in ancestors path - probably error in hierarchy source");
            }
        }
        return ancestors;
    }

    private void cutAncestorsPath(LinkedList<Integer> ancestors, Integer taxid) throws TaxonomyHierarchyException {
        Integer parentid;
        while (!ancestors.contains(taxid)) {
            parentid = db.get(taxid);
            if (parentid == null) {
                throw new TaxonomyHierarchyException("Can't find parent taxon to taxon with id " + taxid);
            }
            taxid = parentid;
        }
        final List<Integer> descenders = new LinkedList<>();
        for (final Integer descender : ancestors) {
            if (descender.equals(taxid)) {
                break;
            }
            descenders.add(descender);
        }
        ancestors.removeAll(descenders);
    }
}
