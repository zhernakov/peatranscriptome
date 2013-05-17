package ngsanalyser.ncbiservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
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
import ngsanalyser.exception.BLASTException;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ncbiservice.blast.BlastHits;
import ngsanalyser.ncbiservice.blast.Hit;
import ngsanalyser.ncbiservice.blast.Hsp;
import ngsanalyser.ngsdata.NGSRecord;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NCBIBLASTService {
    public static final NCBIBLASTService INSTANCE = new NCBIBLASTService();

    private static final SAXParserFactory factory = SAXParserFactory.newInstance();
    
    private static final String blastlink = "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi";
    private static final String elink = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi";
    private final int waitinterval = 30000;
    
    protected NCBIBLASTService() {
    }
    
    private static void printStream(InputStream is) {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException ex) {
        }
    }
    
    private URLConnection send(String statement) throws NoConnectionException {
        final URL url;
        try {
            url = new URL(statement);
        } catch (MalformedURLException ex) {
            throw new NoConnectionException();
        }
        final int attempts = 10;
        for (int attemp = 0; attemp < attempts; ++attemp) {
            try {
                final URLConnection connection = url.openConnection();
                connection.setConnectTimeout(10000);
                return connection;
            } catch (IOException ex) {
                try {
                    Thread.sleep(waitinterval);
                } catch (InterruptedException ex1) {

                }
            }
        }
        throw new NoConnectionException();
    }
    
    private InputStream sendQuery(String statement) throws NoConnectionException {
        try {
            return send(statement).getInputStream();
        } catch (IOException ex) {
            throw new NoConnectionException();
        }
    }  

    //---BLAST---//
    
    public void multiMegaBlast(Collection<NGSRecord> records) throws BLASTException, ParseException, NoConnectionException {
        final String queryId = sendBLASTQuery(records);
        waitQueryResults(queryId);
        final Map<String,BlastHits> hits = parseBlastResult(getQueryResults(queryId));
        sendDeleteRequest(queryId);
        for (final NGSRecord record : records) {
            record.setBLASTHits(hits.get(record.recordid));
        }
    }
    
    private String sendBLASTQuery(Collection<NGSRecord> records) throws BLASTException, NoConnectionException {
        final String statement = composeMultiMegaBlastStatement(records);
        final InputStream in = sendQuery(statement);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.contains("class=\"error\"") && !line.contains("Message ID#")) {
                    if (line.contains("RID = ")) {
                        reader.close();
                        return line.split("=")[1].trim();
                    }
                } else {
                    final String cause = line.split("</p></li></ul>")[0].split("<p class=\"error\">")[1].trim();
                    throw new BLASTException("NCBI QBlast refused this request because: " + cause);
                }
            }
        } catch (IOException ex) {
            throw new NoConnectionException();
        }
        throw new BLASTException("Unable to retrieve request ID");
    }

    private String composeMultiMegaBlastStatement(Collection<NGSRecord> records) {
        final StringBuilder builder = new StringBuilder();
        builder.append(blastlink)
                .append("?CMD=Put&PROGRAM=blastn&MEGABLAST=on&DATABASE=nr&QUERY=");
        for (final NGSRecord record : records) {
            builder.append("%3E")
                    .append(record.recordid)
                    .append("%0D%0A")
                    .append(record.sequence)
                    .append("%0D%0A");
        }
        return builder.toString();
    }

    private void waitQueryResults(String queryId) throws BLASTException, NoConnectionException {
        final String statement = blastlink + "?CMD=Get&RID=" + queryId;
        System.out.println(statement);
        
        while (true) {
            final InputStream in = sendQuery(statement);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.contains("READY")) {
                        reader.close();
                        return;
                    } else if (line.contains("WAITING")) {
                        try {
                            Thread.sleep(this.waitinterval);
                        } catch (InterruptedException ex) {
                            //TODO
                            Logger.getLogger(NCBIBLASTService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        reader.close();
                        break;
                    } else if (line.contains("UNKNOWN")) {
                        reader.close();
                        throw new BLASTException("Unknown request id - no results exist for it. Given id = " + queryId);
                    }
                }
            } catch (IOException ex) {
                throw new NoConnectionException();
            }
        }
    }

    private InputStream getQueryResults(String queryId) throws BLASTException, NoConnectionException {
        final String statement = blastlink + "?CMD=Get&FORMAT_TYPE=XML&RID=" + queryId;
        return sendQuery(statement);
    }
    
    private void sendDeleteRequest(String queryId) throws NoConnectionException {
        final String statement = blastlink + "?CMD=Delete&RID=" + queryId;
        send(statement);
    }

    private Map<String, BlastHits> parseBlastResult(InputStream result) throws ParseException, NoConnectionException {
        try {
            final BlastResultHandler handler = new BlastResultHandler();
            factory.newSAXParser().parse(result, handler);
            return handler.iterations;
        } catch (IOException ex) {
            throw new NoConnectionException();
        } catch (Exception ex) {
            throw new ParseException(ex);
        }
    }
    
    //---ELINK---//
    public Set<Integer> defineTaxonIds(Iterable<String> ids) throws NoConnectionException, ParseException {
        try {
            final String url = composeGetTaxonQuery(ids);
            final InputStream in = sendQuery(url);
            final Set<List<String>> result = parseInputStream(in);
            return getTaxonIdsSet(result); 
        } catch (IOException ex) {
            throw new NoConnectionException();
        } catch (Exception ex) {
            throw new ParseException(ex.getMessage());
        }
    }
        
    private String composeGetTaxonQuery(Iterable<String> ids) {
        String url = elink + "?dbfrom=nucleotide&db=taxonomy&id=";
        for (final String id : ids) {
            url += id + ",";
        }
        return url;
    }

    private Set<List<String>> parseInputStream(InputStream in) throws SAXException, IOException, ParserConfigurationException {
        final ElinkResultHandler handler = new ElinkResultHandler();
        factory.newSAXParser().parse(in, handler);
        return handler.getLinkSet();
    }
    
    private Set<Integer> getTaxonIdsSet(Set<List<String>> result) {
        final Set<Integer> taxids = new HashSet<>();
        for(List<String> link : result) {
            taxids.add(Integer.parseInt(link.get(0)));
        }
        return taxids;
    }

    private class BlastResultHandler extends DefaultHandler {
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
    
    private class ElinkResultHandler extends DefaultHandler {
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
