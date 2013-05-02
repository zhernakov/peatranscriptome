package ngsanalyser.ngsdata;

public class NGSRecord {
    private final String id;
    private final String additional;
    private final String sequence;
    private final String quality;
    
    private String blastresult;

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

    public String getBlastResult() {
        return blastresult;
    }
    
    public void setBlastresult(String blastresult) {
        this.blastresult = blastresult;
    }

}
