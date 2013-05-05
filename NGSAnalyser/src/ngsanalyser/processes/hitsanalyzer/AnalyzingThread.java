package ngsanalyser.processes.hitsanalyzer;

import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParsingException;
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
        final String id = record.getId();
        System.out.println("Hit analysis for " + id + " started.");
        
        try {
            final Iterable<String> seqids = record.getBLASTHits().getSeqIdsSet(DBID.gi, criticalEvalue);
            final Iterable<Integer> taxids = NCBIService.INSTANCE.getTaxIdsSet(seqids);
            final int cmntaxid = Taxonomy.INSTANCE.findCommonAncestor(taxids);
            record.setTaxonId(cmntaxid);
        } catch (NoConnectionException ex) {
            record.connectionLost();
        } catch (ParsingException | TaxonomyHierarchyException ex) {
            record.loqError(ex);
        } finally {
            manager.recordProcessed(record);
            System.out.println("Hit analysis for " + id + " finished. Common ancestor - " + record.getTaxonId());
        }
    }
}
