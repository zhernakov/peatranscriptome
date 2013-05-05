package ngsanalyser.blastresultparser;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import ngsanalyser.ngsdata.NGSRecord;
import org.xml.sax.SAXException;

public class ParsingThread implements Runnable {
    private static final SAXParserFactory factory = SAXParserFactory.newInstance();
    static {
        factory.setValidating(false);
    }

    private final NGSRecord record;
    private final ParserManager manager;

    public ParsingThread(ParserManager manager, NGSRecord record) {
        this.record = record;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            final XMLHandler handler = new XMLHandler();
            final SAXParser parser = factory.newSAXParser();
            parser.parse(new File(record.getBlastResultFilePath()), handler);
            record.setBLASTHits(handler.getResult());
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            record.loqError(ex);
        } catch (Exception ex) {
            record.loqError(ex);
        } finally {
            manager.recordProcessed(record);
        }
    }
    
}
