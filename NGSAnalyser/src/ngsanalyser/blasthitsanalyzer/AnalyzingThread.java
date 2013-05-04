package ngsanalyser.blasthitsanalyzer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.exception.LostConnectionException;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.taxonomy.Taxonomy;
import ngsanalyser.taxonomy.TaxonomyHierarchyException;
import org.biojava.bio.seq.db.IllegalIDException;
import org.biojavax.bio.db.ncbi.GenbankRichSequenceDB;
import org.biojavax.bio.seq.RichSequence;

public class AnalyzingThread implements Runnable {
    private final Taxonomy taxonomy;
    private final GenbankRichSequenceDB grsdb = new GenbankRichSequenceDB();
    private final NGSRecord record;
    private final AnalyzerManager manager;
    private final double criticalEvalue;

    public AnalyzingThread(AnalyzerManager manager, NGSRecord record, Taxonomy taxonomy, double criticalEvalue) {
        this.record = record;
        this.manager = manager;
        this.taxonomy = taxonomy;
        this.criticalEvalue = criticalEvalue;
    }

    @Override
    public void run() {
        try {
            final int taxonid = defineCommonTaxonId();
            record.setTaxonId(taxonid);
        } catch (IllegalIDException | TaxonomyHierarchyException ex) {
            record.loqError(ex);
        } catch (LostConnectionException ex) {
            record.connectionLost();
        } catch (Exception ex) {
            record.loqError(ex);
        } finally {
            manager.recordProcessed(record);
        }
    }

    private int defineCommonTaxonId() throws IllegalIDException, TaxonomyHierarchyException, LostConnectionException {
        final List<Map<String, Object>> hits = record.getBLASTHits();
        final List<Integer> taxids = new LinkedList<>();
        for (final Map<String, Object> hit : hits) {
            final double evalue = defineEvalue(hit.get("Hit_hsps"));
            if (evalue == 0) {
                return defineTaxonId(hit.get("Hit_accession"));
            } else if (evalue < criticalEvalue) {
                taxids.add(defineTaxonId(hit.get("Hit_accession")));
            }
        }
        return taxonomy.findCommonAncestor(taxids);
    }
    
    private double defineEvalue(Object object) {
        final List<Map<String, Object>> hsps = (List<Map<String, Object>>)object;
        double evalue = 1.;
        for (final Map<String, Object> hsp : hsps) {
            double hsp_ev = (Double) hsp.get("Hsp_evalue");
            if (hsp_ev < evalue) {
                evalue = hsp_ev;
            }
        }
        return evalue;
    }

    private int defineTaxonId(Object acession) throws IllegalIDException, LostConnectionException {
        int trycount = 0;
        while (trycount < 10) {
            try {
                final RichSequence rs = grsdb.getRichSequence((String)acession);
                return rs.getTaxon().getNCBITaxID();
            } catch (IllegalIDException ex) {
                throw ex;
            } catch (Exception ex) {
                ++trycount;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(AnalyzingThread.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        throw new LostConnectionException();
    }
}
