package ngsanalyser.processor;

import java.util.List;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class Storaging implements Runnable {
    private final Storager processor;
    private final NGSAddible resultstorage;
    private final NGSAddible failedstorage;
    private final Run run;
    private final List<NGSRecord> records;
    
    private static int count = 0;

    public Storaging(Storager processor, NGSAddible resultstorage, NGSAddible failedstorage, Run run, List<NGSRecord> records) {
        this.processor = processor;
        this.resultstorage = resultstorage;
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
        } catch (Exception ex) {
            System.out.println("Storage operation " + n + " failed");
            failedstorage.addNGSRecordsCollection(records);
        } finally {
            processor.eliminateThread(this);
        }
    }

    public List<NGSRecord> getRecords() {
        return records;
    }
}
