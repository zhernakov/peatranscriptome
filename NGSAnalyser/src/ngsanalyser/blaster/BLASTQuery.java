package ngsanalyser.blaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import ngsanalyser.ngsdata.NGSRecord;
import org.biojava3.core.sequence.io.util.IOUtils;
import org.biojava3.ws.alignment.qblast.BlastProgramEnum;
import org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastOutputProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastService;

public class BLASTQuery implements Runnable {
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
        try {
            final String id = record.getId();
            System.out.println("Blast for " + id + " started.");
            query();
            System.out.println("Blast for " + id + " finished.");
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            manager.recordProcessed(record);
        }
    }

    private void query() {
        String rid = null;
        BufferedReader reader = null;
        FileWriter writer = null;
        try {
            final String outputFilePath = record.getId();
            rid = service.sendAlignmentRequest(record.getSequence(), alignprop);
            while (!service.isReady(rid)) {
                Thread.sleep(5000);
            }
            reader = new BufferedReader(new InputStreamReader(service.getAlignmentResults(rid, outputprop)));
            writer = new FileWriter(new File(outputFilePath));
            
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + System.getProperty("line.separator"));
            }
            record.setBLASTResultFilePath(outputFilePath);
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            IOUtils.close(writer);
            IOUtils.close(reader);
            service.sendDeleteRequest(rid);
        }
    }
    
}
