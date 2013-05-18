package ngsanalyser.ncbiservice.blast;

import java.io.Serializable;

public class Hsp implements Serializable {
    private double  bitscore;
    private int     score;
    private double  evalue;

    public void setBitScore(double Hsp_bitscore) {
        this.bitscore = Hsp_bitscore;
    }

    public void setScore(int Hsp_score) {
        this.score = Hsp_score;
    }

    public void setEValue(double Hsp_evalue) {
        this.evalue = Hsp_evalue;
    }

    public double getBitScore() {
        return bitscore;
    }

    public int getScore() {
        return score;
    }

    public double getEValue() {
        return evalue;
    }
}
