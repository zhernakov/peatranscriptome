package ngsanalyser.ncbiservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ncbiservice.blast.BlastHits;
import ngsanalyser.ncbiservice.blast.Hit;
import ngsanalyser.ncbiservice.blast.Hsp;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NCBIParser {
    private static final SAXParserFactory factory = SAXParserFactory.newInstance();
    

    protected NCBIParser() {
    }
    
    public static Map<String,BlastHits> parseBlastResult(InputStream is) throws NoConnectionException, ParseException {
        try {
            final BlastResultHandler handler = new BlastResultHandler();
            factory.newSAXParser().parse(is, handler);
            return handler.iterations;
        } catch (IOException ex) {
            throw new NoConnectionException();
        } catch (Exception ex) {
            throw new ParseException(ex);
        }
        
    }

    public static Set<List<String>> parseEUtilsResult(InputStream in) throws NoConnectionException, ParseException {
        try {
            final EUtilsResultHandler handler = new EUtilsResultHandler();
            factory.newSAXParser().parse(in, handler);
            return handler.getLinkSet();
        } catch (IOException ex) {
            throw new NoConnectionException();
        } catch (Exception ex) {
            throw new ParseException(ex);
        }
    }

    private static class BlastResultHandler extends DefaultHandler {
        private Map<String,BlastHits> iterations = new TreeMap<>();

        private BlastHits iteration;
        private Hit hit;
        private Hsp hsp;
        private StringBuilder buffer;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case "Iteration":
                    iteration = new BlastHits();
                    break;
                case "Iteration_query-def":
                    buffer = new StringBuilder();
                    break;
                case "Hit":
                    hit = new Hit();
                    iteration.addHit(hit);
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
                case "Iteration":
                    iteration = null;
                    break;
                case "Iteration_query-def":
                    iterations.put(buffer.toString(), iteration);
                    buffer = null;
                    break;
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
    
    private static class EUtilsResultHandler extends DefaultHandler {
        private final Set<List<String>> linkset = new HashSet<>();
        
        private List<String> link;
        private StringBuffer buffer;

        private boolean linksetdb = false;

        public Set<List<String>> getLinkSet() {
            return linkset;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case "LinkSetDb": 
                    linksetdb = true;
                    break;
                case "Link":
                    if (linksetdb) {
                        link = new LinkedList<>();
                        linkset.add(link);
                    }
                    break;
                case "Id":
                    if (link != null) {
                        buffer = new StringBuffer();
                    }
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case "LinkSetDb": 
                    linksetdb = false;
                    break;
                case "Link":
                    if (linksetdb) {
                        link = null;
                    }
                    break;
                case "Id":
                    if (buffer != null) {
                        link.add(buffer.toString());
                        buffer = null;
                    }
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
}
