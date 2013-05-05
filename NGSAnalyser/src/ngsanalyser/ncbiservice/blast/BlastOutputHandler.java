package ngsanalyser.ncbiservice.blast;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BlastOutputHandler extends DefaultHandler {

    private BlastHits hits = new BlastHits();
    private Hit hit;
    private Hsp hsp;
    private StringBuilder buffer;

    public BlastHits getResult() {
        return hits;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "Hit":
                hit = new Hit();
                hits.addHit(hit);
                break;
            case "Hit_id":
            case "Hit_def":
            case "Hit_accession":
                buffer = new StringBuilder();
                break;
            case "Hsp":
                hsp = new Hsp();
                hit.addHsp(hsp);
                break;
            case "Hsp_bit-score":
            case "Hsp_score":
            case "Hsp_evalue":
                buffer = new StringBuilder();
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "Hit":
                hit = null;
                break;
            case "Hit_id":
                hit.setIdentifiers(buffer.toString());
                buffer = null;
                break;
            case "Hit_def":
                hit.setDefenition(buffer.toString());
                buffer = null;
                break;
            case "Hit_accession":
                hit.setAccession(buffer.toString());
                buffer = null;
                break;
            case "Hsp":
                hsp = null;
                break;
            case "Hsp_bit-score":
                hsp.setBitScore(Double.parseDouble(buffer.toString()));
                buffer = null;
                break;
            case "Hsp_score":
                hsp.setScore(Integer.parseInt(buffer.toString()));
                buffer = null;
                break;
            case "Hsp_evalue":
                hsp.setEValue(Double.parseDouble(buffer.toString()));
                buffer = null;
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (buffer != null) {
            buffer.append(ch, start, length);
        }
    }
}
