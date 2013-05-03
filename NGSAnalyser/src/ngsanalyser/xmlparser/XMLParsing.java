package ngsanalyser.xmlparser;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import ngsanalyser.ngsdata.NGSRecord;
import org.xml.sax.SAXException;

public class XMLParsing implements Runnable {
    private static final SAXParserFactory factory = SAXParserFactory.newInstance();
    static {
        factory.setValidating(false);
    }

    private final NGSRecord record;
    private final XMLParserManager manager;

    public XMLParsing(XMLParserManager manager, NGSRecord record) {
        this.record = record;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            final XMLParserHandler handler = new XMLParserHandler();
            final SAXParser parser = factory.newSAXParser();
            parser.parse(new File(record.getBlastResult()), handler);
            record.setBLASTParsing(handler.getResult());
            manager.finishParsing(record);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLParsing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(XMLParsing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLParsing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
