package ngsanalyser.processes.blaster;

import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParseException;
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
        System.out.println("Blast for " + record.recordid + " started.");

        try {
            record.setBLASTHits(NCBIService.INSTANCE.blast(record.sequence));
        } catch (NoConnectionException ex) {
            record.connectionLost();
        } catch (ParseException ex) {
            record.loqError(ex);
        } catch (Exception ex) {
            record.loqError(ex);
        } finally {
            manager.recordProcessed(record);
            System.out.println("Blast for " + record.recordid + " finished.");
        }
    }
}
