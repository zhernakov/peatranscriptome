package ngsanalyser.processor2;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ncbiservice.NCBIParser;
import ngsanalyser.ncbiservice.NCBIService2;
import ngsanalyser.ncbiservice.blast.DBID;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.taxonomy.Taxonomy;
import ngsanalyser.taxonomy.TaxonomyException;

public class HitsAnalyzer extends AbstractProcessor {
    private final double evalue;

    public HitsAnalyzer(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber, double evalue) {
        super("HitsAnalyzer", resultstorage, failedstorage, threadnumber);
        this.evalue = evalue;
    }

    @Override
    public void addNGSRecord(NGSRecord record) {
        startNewProcess(new HitsScan(record));
    }

    //////////////////
    
    private class HitsScan extends Process {
        private final NGSRecord record;

        public HitsScan(NGSRecord record) {
            this.record = record;
        }

        @Override
        protected void processing() throws NoConnectionException, ParseException, TaxonomyException {
            int cmntaxid = 1;
            final Collection<String> seqids = record.getBLASTHits().getSeqIdsSet(DBID.gi, evalue);
            if (!seqids.isEmpty()) {
                final InputStream stream = NCBIService2.INSTANCE.defineTaxonsSet(seqids);
                final Set<List<String>> result = NCBIParser.parseEUtilsResult(stream);
                final Set<Integer> taxids = getTaxonIdsSet(result);
                cmntaxid = Taxonomy.INSTANCE.findCommonAncestor(taxids);
            }
            record.setTaxonId(cmntaxid);
        }

        @Override
        protected Process cloneProcess() {
            return new HitsScan(record);
        }

        @Override
        protected Collection<NGSRecord> getRecords() {
            final List<NGSRecord> records = new LinkedList<>();
            records.add(record);
            return records;
        }
        
        private Set<Integer> getTaxonIdsSet(Set<List<String>> result) {
            final Set<Integer> taxids = new HashSet<>();
            for (List<String> link : result) {
                taxids.add(Integer.parseInt(link.get(0)));
            }
            return taxids;
        }
    }
}
