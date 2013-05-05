package ngsanalyser.blasthitsanalyzer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ngsanalyser.exception.LostConnectionException;
import ngsanalyser.exception.ParsingException;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.taxonomy.Taxonomy;
import ngsanalyser.taxonomy.TaxonomyHierarchyException;

public class AnalyzingThread implements Runnable {
    private static final NCBIQueryService ncbiservice = new NCBIQueryService();
    
    private final Taxonomy taxonomy;
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
        } catch (LostConnectionException ex) {
            record.connectionLost();
        } catch (ParsingException | TaxonomyHierarchyException ex) {
            record.loqError(ex);
        } finally {
            manager.recordProcessed(record);
        }
    }

    private int defineCommonTaxonId() throws LostConnectionException, ParsingException, TaxonomyHierarchyException {
        final List<String> seqids = selectHits(record.getBLASTHits(), criticalEvalue);
        if (seqids.isEmpty()) return 1;
        final Set<Integer> taxonids = ncbiservice.defineTaxonIds(seqids);
        if (taxonids.isEmpty()) return 1;
        final int commonid = taxonomy.findCommonAncestor(taxonids);
        return commonid;
    }
    
    private static List<String> selectHits(List<Map<String, Object>> blastHits, double criticalEvalue) {
        final List<String> seqids = new LinkedList<>();
        for (final Map<String, Object> hit : blastHits) {
            final double evalue = getMinimalEValue(hit.get("Hit_hsps"));
            if (evalue == 0) {
                seqids.clear();
                seqids.add(getHitGeneId(hit.get("Hit_id")));
                break;
            } else if (evalue < criticalEvalue) {
                seqids.add(getHitGeneId(hit.get("Hit_id")));
            }
        }
        return seqids;
    }
    
    private static double getMinimalEValue(Object object) {
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

    private static String getHitGeneId(Object obj) {
        final Map<String,List<String>> ids = (Map<String,List<String>>) obj;
        return ids.get("gi").get(0);
    }
}
