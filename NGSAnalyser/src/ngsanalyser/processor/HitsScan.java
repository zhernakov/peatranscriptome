package ngsanalyser.processor;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.ncbiservice.NCBIService;
import ngsanalyser.ncbiservice.blast.DBID;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.taxonomy.Taxonomy;

public class HitsScan implements Runnable {
    private final HitsAnalyzer processor;
    private final NGSRecord record;
    private final double evalue;
            
    HitsScan(HitsAnalyzer processor, NGSRecord record, double evalue) {
        this.processor = processor;
        this.record = record;
        this.evalue = evalue;
    }

    NGSRecord getRecord() {
        return record;
    }

    @Override
    public void run() {
        try {
            System.out.println("HitAnalysis for read " + record.recordid + " started");
            int cmntaxid = 1;
            final Collection<String> seqids = record.getBLASTHits().getSeqIdsSet(DBID.gi, evalue);
            if (!seqids.isEmpty()) {
                final Collection<Integer> taxids = NCBIService.INSTANCE.getTaxIdsSet(seqids);
                cmntaxid = Taxonomy.INSTANCE.findCommonAncestor(taxids);
            }
            record.setTaxonId(cmntaxid);
            System.out.println("HitAnalysis for read " + record.recordid + " finished successfully");
            processor.threadSuccessfullyFinished(this);
        } catch (Exception ex) {
            Logger.getLogger(HitsScan.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("HitAnalysis for read " + record.recordid + " faied");
            processor.threadProcessingFailed(this, ex);
        }
    }
}
