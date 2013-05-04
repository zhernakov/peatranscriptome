package ngsanalyser.ngsdata;

import java.util.List;
import java.util.Map;

public class NGSRecord {
    private final String id;
    private final String additional;
    private final String sequence;
    private final String quality;
    
    private String blastresultfilepath;
    private List<Map<String, Object>> blasthits;
    private int taxid;

    public NGSRecord(String id, String additional, String sequence, String quality) {
        this.id = id;
        this.additional = additional;
        this.sequence = sequence;
        this.quality = quality;
    }

    NGSRecord(String description, String sequence, String quality) {
        int gap = description.indexOf(" ");
        id = description.substring(0, gap);
        additional = description.substring(gap + 1);
        this.sequence = sequence;
        this.quality = quality;
    }

    public String getId() {
        return id;
    }

    public String getAdditional() {
        return additional;
    }

    public String getSequence() {
        return sequence;
    }

    public String getQuality() {
        return quality;
    }

    public String getBlastResultFilePath() {
        return blastresultfilepath;
    }
  
    public List<Map<String, Object>> getBLASTHits() {
        return blasthits;
    }
  
    public int getTaxonId() {
        return taxid;
    }

    public void setBLASTResultFilePath(String blastresult) {
        this.blastresultfilepath = blastresult;
    }

    public void setBLASTHits(List<Map<String, Object>> hits) {
        blasthits = hits;
    }

    public void setTaxonId(int taxid) {
        this.taxid = taxid;
    }

}
