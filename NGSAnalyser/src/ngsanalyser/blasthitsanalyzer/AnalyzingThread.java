package ngsanalyser.blasthitsanalyzer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ngsanalyser.ngsdata.NGSRecord;

public class AnalyzingThread implements Runnable {
    private final NGSRecord record;
    private final AnalyzerManager manager;
    private final double criticalEvalue;

    public AnalyzingThread(AnalyzerManager manager, NGSRecord record, double criticalEvalue) {
        this.record = record;
        this.manager = manager;
        this.criticalEvalue = criticalEvalue;
    }

    @Override
    public void run() {
        final List<Map<String, Object>> hits = record.getBLASTHits();
        final List<Integer> taxids = new LinkedList<Integer>();
        for (final Map<String, Object> hit : hits) {
            final double evalue = defineEvalue(hit.get("Hit_hsps"));
            if (evalue == 0) {
                record.setTaxonId(defineTaxonId(hit.get("Hit_accession")));
                manager.recordProcessed(record);
                return;
            } else if (evalue < criticalEvalue) {
                taxids.add(defineTaxonId(hit.get("Hit_accession")));
            }
        }
        record.setTaxonId(defineCommonAncestor(taxids));
        manager.recordProcessed(record);
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

    private int defineTaxonId(Object acession) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private int defineCommonAncestor(List<Integer> taxids) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
