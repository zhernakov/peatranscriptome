package ngsanalyser.ncbiservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ncbiservice.blast.BlastHits;
import ngsanalyser.ncbiservice.blast.BlastOutputHandler;
import org.biojava3.ws.alignment.qblast.BlastProgramEnum;
import org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastOutputProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastService;

public class NCBIService {
    public static final NCBIService INSTANCE = new NCBIService(); 
    private NCBIService() {
    }

    private static final NCBIQBlastService blastservice = new NCBIQBlastService();
    private static final NCBIQBlastAlignmentProperties alignprop = new NCBIQBlastAlignmentProperties();
    private static final NCBIQBlastOutputProperties outputprop = new NCBIQBlastOutputProperties();

    private static final NCBIQueryService ncbiservice = new NCBIQueryService();
    
    private static final SAXParserFactory parserfactory = SAXParserFactory.newInstance();
    
    private static final Timer timer = new Timer(250);

    static {
        alignprop.setBlastProgram(BlastProgramEnum.megablast);
        alignprop.setBlastDatabase("nr");
    }
    
    public BlastHits blast(String sequence) throws NoConnectionException, ParseException {
        timer.start();
        final InputStream is = sendBLASTQuery(sequence);
        return parseBLASTResult(is);
    }
    
    private InputStream sendBLASTQuery(String sequence) throws NoConnectionException {
        try {
            final String rid = blastservice.sendAlignmentRequest(sequence, alignprop);
            while (!blastservice.isReady(rid)) {
                Thread.sleep(5000);
            }
            return blastservice.getAlignmentResults(rid, outputprop);
        } catch (Exception ex) {
            throw new NoConnectionException(ex.getMessage());
        }
    }
    
    private BlastHits parseBLASTResult(InputStream is) throws NoConnectionException, ParseException {
        try {
            final BlastOutputHandler handler = new BlastOutputHandler();
            final SAXParser parser = parserfactory.newSAXParser();
            parser.parse(is, handler);
            return handler.getResult();
        } catch (IOException ex) {
            throw new NoConnectionException(ex.getMessage());
        } catch (Exception ex) {
            throw new ParseException(ex.getMessage());
        }
    }
    
    public InputStream sendELinkQuery() {
        return null;
    }

    public Collection<Integer> getTaxIdsSet(Iterable<String> seqids) throws NoConnectionException, ParseException {
        timer.start();
        return ncbiservice.defineTaxonIds(seqids);
    }
}
