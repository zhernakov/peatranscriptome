package ngsanalyser.ncbiservice.blast;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BlastHits {
    
    private List<Hit> hits = new LinkedList<>();
    
    public void addHit(Hit hit) {
        hits.add(hit);
    }

    public Collection<String> getSeqIdsSet(DBID dbid, double criticalEvalue) {
        final Set<String> seqids = new HashSet<>();
        for (final Hit hit : hits) {
            final double evalue = hit.getMinimalEValue();
            if (evalue == 0) {
                seqids.clear();
                seqids.addAll(hit.getSeqIds(dbid));
                break;
            } else if (evalue < criticalEvalue) {
                seqids.addAll(hit.getSeqIds(dbid));
            }
        }
        return seqids;
    }
}
