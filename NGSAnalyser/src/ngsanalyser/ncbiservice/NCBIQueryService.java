package ngsanalyser.ncbiservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParseException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NCBIQueryService {
    private static final SAXParserFactory factory = SAXParserFactory.newInstance();
    private static final String elink = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi";
    private final int waitinterval = 10000;

    public Set<Integer> defineTaxonIds(Iterable<String> ids) throws NoConnectionException, ParseException {
        try {
            final String url = composeQueryStatement(ids);
            final InputStream in = openURL(new URL(url));
            final Set<List<String>> result = parseInputStream(in);
            return getTaxonIdsSet(result); 
        } catch (IOException ex) {
            throw new NoConnectionException();
        } catch (Exception ex) {
            throw new ParseException(ex.getMessage());
        }
    }

    private String composeQueryStatement(Iterable<String> ids) {
        String url = elink + "?dbfrom=nucleotide&db=taxonomy&id=";
        for (final String id : ids) {
            url += id + ",";
        }
        return url;
    }
    
    private Set<Integer> getTaxonIdsSet(Set<List<String>> result) {
        final Set<Integer> taxids = new HashSet<>();
        for(List<String> link : result) {
            taxids.add(Integer.parseInt(link.get(0)));
        }
        return taxids;
    }
    
    private InputStream openURL(URL url) throws IOException {
        final int attempts = 10;
        for (int attemp = 0; attemp < attempts; ++attemp) {
            try {
                final URLConnection connection = url.openConnection();
                connection.setConnectTimeout(10000);
                return connection.getInputStream();
            } catch (IOException ex) {
                try {
                    Thread.sleep(waitinterval);
                } catch (InterruptedException ex1) {

                }
            }
        }
        throw new IOException("Cannot open " + url);
    }

    private void printInputStream(InputStream in) throws IOException {
        int c;
        byte[] buffer = new byte[2048];
        while ((c = in.read(buffer)) != -1) {
            System.out.write(buffer, 0, c);
        }
    }

    private Set<List<String>> parseInputStream(InputStream in) throws SAXException, IOException, ParserConfigurationException {
        final QueryHandler handler = new QueryHandler();
        factory.newSAXParser().parse(in, handler);
        return handler.getLinkSet();
    }

    private class QueryHandler extends DefaultHandler {
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
