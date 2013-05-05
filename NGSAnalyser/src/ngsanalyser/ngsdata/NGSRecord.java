package ngsanalyser.ngsdata;

import java.util.List;
import java.util.Map;
import ngsanalyser.ncbiservice.blast.BlastHits;

public class NGSRecord {
    private final String recordid;
    private final String additionalinfo;
    private final String sequence;
    private final String quality;
    
    private BlastHits blasthits;
    private int taxid = -1;
    
    private boolean connectionlost = false;
    private StringBuffer logger = new StringBuffer ();

    public NGSRecord(String id, String additional, String sequence, String quality) {
        this.recordid = id;
        this.additionalinfo = additional;
        this.sequence = sequence;
        this.quality = quality;
    }

    public NGSRecord(String description, String sequence, String quality) {
        int gap = description.indexOf(" ");
        recordid = description.substring(0, gap);
        additionalinfo = description.substring(gap + 1);
        this.sequence = sequence;
        this.quality = quality;
    }

    public boolean isConnectionLost() {
        return connectionlost;
    }

    public void connectionLost() {
        connectionlost = true;
    }
    
    public void resetConnectionFlag() {
        connectionlost = false;
    }

    public void loqError(Exception e) {
        logger.append(e.getMessage());
        logger.append("\n");
    }
    
    public String getId() {
        return recordid;
    }

    public String getAdditional() {
        return additionalinfo;
    }

    public String getSequence() {
        return sequence;
    }

    public String getQuality() {
        return quality;
    }

    public BlastHits getBLASTHits() {
        return blasthits;
    }
  
    public int getTaxonId() {
        return taxid;
    }

    public void setBLASTHits(BlastHits hits) {
        blasthits = hits;
    }

    public void setTaxonId(int taxid) {
        this.taxid = taxid;
    }

}
