package taxonomy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.bio.taxa.io.SimpleNCBITaxonomyLoader;

public class Taxonomy {
    
    public static void main(String[] args) throws FileNotFoundException, ParseException, IOException {
        final File src = new File(args[1]);
        final File outputdir = new File (args.length > 1 ? args[2] : ""); 

        switch (args[0]) {
            case "r":
                relationships(src, new File(outputdir, "taxonomy_rs"));
                break;
            case "n":
                break;
            default:
                System.err.println("Unknown command " + args[0]);
                break;
        }
    }
    
    private static void relationships(File src, File out) throws FileNotFoundException, ParseException, IOException {
        final SimpleNCBITaxonomyLoader loader = new SimpleNCBITaxonomyLoader();
        final BufferedReader reader = new BufferedReader(new FileReader(src));
        final TaxonRS db = new TaxonRS();
        NCBITaxon taxon;
        while ((taxon = loader.readNode(reader)) != null) {
            db.add(taxon.getNCBITaxID(), taxon.getParentNCBITaxID());
        }
    }
}
