package ngsanalyser.processes.hitsanalyzer;

import java.util.Collection;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ncbiservice.NCBIService;
import ngsanalyser.ncbiservice.blast.DBID;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.taxonomy.Taxonomy;
import ngsanalyser.taxonomy.TaxonomyHierarchyException;

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
        System.out.println("Hits analysis for " + record.recordid + " started.");
        
        try {
            int cmntaxid = 1;
            final Collection<String> seqids = record.getBLASTHits().getSeqIdsSet(DBID.gi, criticalEvalue);
            if (!seqids.isEmpty()) {
                final Collection<Integer> taxids = NCBIService.INSTANCE.getTaxIdsSet(seqids);
                cmntaxid = Taxonomy.INSTANCE.findCommonAncestor(taxids);
            }
            record.setTaxonId(cmntaxid);
        } catch (NoConnectionException ex) {
            record.connectionLost();
        } catch (ParseException | TaxonomyHierarchyException ex) {
            record.loqError(ex);
        } catch (Exception ex) {
            record.loqError(ex);
        } finally {
            manager.recordProcessed(record);
            System.out.println("Hits analysis for " + record.recordid + " finished. Common ancestor - " + record.getTaxonId());
        }
    }
}
