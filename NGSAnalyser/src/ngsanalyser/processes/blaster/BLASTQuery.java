package ngsanalyser.processes.blaster;

import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParsingException;
import ngsanalyser.ncbiservice.NCBIService;
import ngsanalyser.ngsdata.NGSRecord;

public class BLASTQuery implements Runnable {
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

        try {
            record.setBLASTHits(NCBIService.INSTANCE.blast(record.getSequence()));
        } catch (NoConnectionException ex) {
            record.connectionLost();
        } catch (ParsingException ex) {
            record.loqError(ex);
        } finally {
            manager.recordProcessed(record);
            System.out.println("Blast for " + id + " finished.");
        }
    }
}
