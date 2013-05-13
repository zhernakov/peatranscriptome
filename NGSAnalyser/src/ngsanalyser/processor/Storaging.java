package ngsanalyser.processor;

import java.util.List;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class Storaging implements Runnable {
    protected static int recordsprocessed = 0;
    protected static int recordsfailed = 0;

    private final Storager processor;
    private final NGSAddible failedstorage;
    private final Run run;
    private final List<NGSRecord> records;
    
    private static int count = 0;

    public Storaging(Storager processor, NGSAddible failedstorage, Run run, List<NGSRecord> records) {
        this.processor = processor;
        this.failedstorage = failedstorage;
        this.run = run;
        this.records = records;
    }

    @Override
    public void run() {
        int n = ++count;
        try {
//            System.out.println("Storage operation " + n + " started");
            DBService.INSTANCE.addSequences(run, records);
//            System.out.println("Storage operation " + n + " finished");
            recordsprocessed += records.size();
        } catch (Exception ex) {
            System.out.println("Storage operation " + n + " failed");
            recordsfailed += records.size();
            failedstorage.addNGSRecordsCollection(records);
        } finally {
            processor.eliminateThread(this);
        }
    }

    public List<NGSRecord> getRecords() {
        return records;
    }
}
