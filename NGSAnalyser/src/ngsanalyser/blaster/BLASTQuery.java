package ngsanalyser.blaster;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import ngsanalyser.blastresultparser.XMLHandler;
import ngsanalyser.ngsdata.NGSRecord;
import org.biojava3.ws.alignment.qblast.BlastProgramEnum;
import org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastOutputProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastService;

public class BLASTQuery implements Runnable {

    private static final SAXParserFactory parserfactory = SAXParserFactory.newInstance();
    private static final NCBIQBlastService service = new NCBIQBlastService();
    private static final NCBIQBlastAlignmentProperties alignprop = new NCBIQBlastAlignmentProperties();
    private static final NCBIQBlastOutputProperties outputprop = new NCBIQBlastOutputProperties();

    static {
        alignprop.setBlastProgram(BlastProgramEnum.megablast);
        alignprop.setBlastDatabase("nr");
    }
    
    private final NGSRecord record;
    private final BLASTManager manager;

    public BLASTQuery(BLASTManager manager, NGSRecord record) {
        this.record = record;
        this.manager = manager;
    }

    @Override
    public void run() {
        final String id = record.getId();
        System.out.println("Blast for " + id + " started.");

        final InputStream is = blast();
        if (is != null) {
            final List<Map<String, Object>> hits = parse(is);
            record.setBLASTHits(hits);
        }
        manager.recordProcessed(record);

        System.out.println("Blast for " + id + " finished.");
    }

    private InputStream blast() {
        try {
            final String rid = service.sendAlignmentRequest(record.getSequence(), alignprop);
            while (!service.isReady(rid)) {
                Thread.sleep(5000);
            }
            return service.getAlignmentResults(rid, outputprop);
        } catch (Exception ex) {
            record.connectionLost();
            return null;
        }
    }

    private List<Map<String, Object>> parse(InputStream is) {
        try {
            final XMLHandler handler = new XMLHandler();
            final SAXParser parser = parserfactory.newSAXParser();
            parser.parse(is, handler);
            return handler.getResult();
        } catch (IOException er) {
            record.connectionLost();
        } catch (Exception ex) {
            record.loqError(ex);
        }
        return null;
    }
}
