package ngsanalyser.ngsdata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.exception.BLASTException;
import ngsanalyser.ncbiservice.blast.BlastHits;

public class NGSRecord {
    public final String recordid;
    public final String additionalinfo;
    public final String sequence;
    public final String quality;
    public final int length;
    
    private BlastHits blasthits;
    private int taxid = -1;
    
    private boolean connectionlost = false;
    private StringBuffer logger = new StringBuffer ();

    public NGSRecord(String id, String additional, String sequence, String quality) {
        this.recordid = id;
        this.additionalinfo = additional;
        this.sequence = sequence;
        this.quality = quality;
        this.length = sequence.length();
        //TODO check if lenghts of sequences and quality strings are same
    }

    public NGSRecord(String description, String sequence, String quality) {
        int gap = description.indexOf(" ");
        recordid = description.substring(0, gap);
        additionalinfo = description.substring(gap + 1);
        this.sequence = sequence;
        this.quality = quality;
        this.length = sequence.length();
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
    
    public BlastHits getBLASTHits() {
        return  blasthits;
    }
  
    public InputStream getBLASTHitsSerialized() throws BLASTException {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(blasthits);
            oos.flush();
            oos.close();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException ex) {
            //TODO
            Logger.getLogger(NGSRecord.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new BLASTException("Can't serialize BLASTHits");
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

    public String getExceptionMessage() {
        return logger.toString();
    }
}
