package ngsanalyser.processor;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.ncbiservice.NCBIService;
import ngsanalyser.ncbiservice.blast.DBID;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.taxonomy.Taxonomy;

public class HitsScan implements Runnable {
    private final HitsAnalyzer processor;
    private final NGSAddible resultstorage;
    private final NGSAddible failedstorage;
    private final double evalue;

    private final NGSRecord record;

    public HitsScan(HitsAnalyzer processor, NGSAddible resultstorage, NGSAddible failedstorage, double evalue, NGSRecord record) {
        this.processor = processor;
        this.resultstorage = resultstorage;
        this.failedstorage = failedstorage;
        this.evalue = evalue;
        this.record = record;
    }

    @Override
    public void run() {
        try {
//            System.out.println("HitAnalysis for read " + record.recordid + " started");
            int cmntaxid = 1;
            final Collection<String> seqids = record.getBLASTHits().getSeqIdsSet(DBID.gi, evalue);
            if (!seqids.isEmpty()) {
                final Collection<Integer> taxids = NCBIService.INSTANCE.getTaxIdsSet(seqids);
                cmntaxid = Taxonomy.INSTANCE.findCommonAncestor(taxids);
            }
            record.setTaxonId(cmntaxid);
//            System.out.println("HitAnalysis for read " + record.recordid + " finished successfully");
            resultstorage.addNGSRecord(record);
            processor.eliminateThread(this);
        } catch (NoConnectionException ex) {
            try {
                wait(5000);
            } catch (InterruptedException ex1) {
                Logger.getLogger(BLASTQuery.class.getName()).log(Level.SEVERE, null, ex1);
            }
            processor.restartThread(this);
        } catch (Exception ex) {
            Logger.getLogger(HitsScan.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("HitAnalysis for read " + record.recordid + " faied");
            failedstorage.addNGSRecord(record);
            processor.eliminateThread(this);
        }
    }
}
