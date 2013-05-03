package ngsanalyser.xmlparser;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParserHandler extends DefaultHandler {
    private final List<Map<String,Object>> hits = new LinkedList<>();
    
    private Map<String,Object> hit;
    private List<Map<String,Object>> hsps;
    private Map<String,Object> hsp;
    
    private StringBuffer buffer;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "Hit":
                hit = new LinkedHashMap<>();
                hits.add(hit);
                break;
            case "Hit_hsps":
                hsps = new LinkedList<>();
                hit.put(qName, hsps);
                break;
            case "Hsp":
                hsp = new LinkedHashMap<>();
                hsps.add(hsp);
                break;
            case "Hit_id":
            case "Hit_def":
            case "Hit_accession":
            case "Hsp_bit-score":
            case "Hsp_score":
            case "Hsp_evalue":
                buffer = new StringBuffer();
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
            case "Hit_def":
            case "Hit_accession":
                hit.put(qName, buffer.toString());
                buffer = null;
                break;
            case "Hit_hsps":
                hsps = null;
                break;
            case "Hsp":
                hsp = null;
                break;
            case "Hsp_bit-score":
            case "Hsp_evalue":
                hsp.put(qName, Double.parseDouble(buffer.toString()));
                buffer = null;
                break;
            case "Hsp_score":
                hsp.put(qName, Integer.parseInt(buffer.toString()));
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

    public List<Map<String, Object>> getResult() {
        return hits;
    }
}
