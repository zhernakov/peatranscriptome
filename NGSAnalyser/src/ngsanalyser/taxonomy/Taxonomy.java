package ngsanalyser.taxonomy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.NoDataBaseResponseException;

public class Taxonomy {
    public static final Taxonomy INSTANCE = new Taxonomy();

    private final Map<Integer, Integer> taxons = new TreeMap<>();

    private Taxonomy(){
    }
    
     
    public void loadData(String path) throws TaxonomyException, SQLException, NoDataBaseResponseException {
        if (path == null){
            INSTANCE.loadDataFromDataBase();
        }else{
            INSTANCE.loadDataFromFile(path);
        }
    }
    
    public void loadDataFromDataBase() throws SQLException, NoDataBaseResponseException {
        DBService.INSTANCE.copyTaxonomy(taxons);
    }

    public void loadDataFromFile(String path) throws TaxonomyException {
        FileInputStream in = null;
        final byte[] array = new byte[6];

        try {
            in = new FileInputStream(new File(path));
            while (in.read(array) != -1) {
                int taxid = ((((int) array[0]) & 0xFF) << 16) | ((((int) array[1]) & 0xFF) << 8) | (((int) array[2]) & 0xFF);
                int parent = ((((int) array[3]) & 0xFF) << 16) | ((((int) array[4]) & 0xFF) << 8) | (((int) array[5]) & 0xFF);
                Integer put = taxons.put(taxid, parent);
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
            throw new TaxonomySrcFileException("Logical problem while reading taxons hierarchy file " + path
                    + " propably file structure is corupted");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
            }
        }
    }

//    /////////////////////////////////////////
    
    public int getTaxonNumber() {
        return taxons.size();
    }

    public boolean hasTaxon(Integer taxid) {
        return taxons.containsKey(taxid);
    }

    public int findCommonAncestor(Iterable<Integer> list) throws TaxonomyHierarchyException {
        final Iterator<Integer> it = list.iterator();
        if (!it.hasNext()) {
            return 1;
        } else {
            final LinkedList<Integer> ancestors = defineAncestorsPath(it.next());
            while (it.hasNext()) {
                final Integer member = it.next();
                cutAncestorsPath(ancestors, member);
            }
            return ancestors.get(0);
        }
    }

    private LinkedList<Integer> defineAncestorsPath(Integer taxid) throws TaxonomyHierarchyException {
        final LinkedList<Integer> ancestors = new LinkedList<>();
        ancestors.add(taxid);
        Integer parentid;
        while ((parentid = taxons.get(taxid)) != taxid) {
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
            parentid = taxons.get(taxid);
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
