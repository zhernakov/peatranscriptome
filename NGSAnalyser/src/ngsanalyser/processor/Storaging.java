package ngsanalyser.processor;

import java.util.List;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSRecord;

public class Storaging implements Runnable {
    private final Storager processor;
    private final List<NGSRecord> records;
    private final Run run;
    
    private static int count = 0;
    
    Storaging(Storager processor, List<NGSRecord> records, Run run) {
        this.processor = processor;
        this.records = records;
        this.run = run;
    }

    @Override
    public void run() {
        int n = ++count;
        try {
//            System.out.println("Storage operation " + n + " started");
            DBService.INSTANCE.addSequences(run, records);
//            System.out.println("Storage operation " + n + " finished");
            processor.insertCompleted(this);
        } catch (Exception ex) {
            System.out.println("Storage operation " + n + " failed");
            processor.insertFailed(this, ex);
        }
    }

    public List<NGSRecord> getRecords() {
        return records;
    }
}
