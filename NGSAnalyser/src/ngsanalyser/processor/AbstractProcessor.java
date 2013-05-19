package ngsanalyser.processor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.exception.BLASTException;
import ngsanalyser.exception.NCBIConnectionException;
import ngsanalyser.exception.DataBaseResponseException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.taxonomy.TaxonomyException;

public abstract class AbstractProcessor implements NGSAddible {
    private final String processorname;
    
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private int threadnumber;
    private int threadsinwork = 0;

    private int startedthreads = 0;
    private int successfullthreads = 0;
    private int restartedthreads = 0;
    private int failedthreads = 0;

    private int successfullrecords = 0;
    private int failedrecords = 0;

    private final int waitingsize = 50;
    private final long[] waiting = new long[waitingsize];
    private int waitingcursor = 0;
    private long waitingsum = 0;

    protected final NGSAddible resultstorage;
    protected final NGSAddible failedstorage;

    public AbstractProcessor(String name, NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
        this.processorname = name;
        this.resultstorage = resultstorage;
        this.failedstorage = failedstorage;
        this.threadnumber = threadnumber;
    }

    @Override
    public abstract void addNGSRecord(NGSRecord record);
    
    @Override
    public final void addNGSRecordsCollection(Collection<NGSRecord> records) {
        for(final NGSRecord record : records) {
            addNGSRecord(record);
        }
    }

    @Override
    public void terminate() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                initiateTermination();
            }
        })).start();
    }
    
    private synchronized void initiateTermination() {
        while (threadsinwork != 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                //TODO
                Logger.getLogger(AbstractProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        executor.shutdown();
        if (resultstorage != null) {
            resultstorage.terminate();
        } else {
            failedstorage.terminate();
        }
        System.out.println("Processor " + processorname +" is terminated:"
                + "\n\tstarted threads:" + startedthreads
                + "\n\tsuccessfull threads:" + successfullthreads 
                + "\n\ttreated records: " + successfullrecords);
    }
    

    @Override
    public int getNumber() {
        return successfullrecords + failedrecords;
    }

///////////
    
    protected synchronized void startNewProcess(Process process) {
        try {
            long start = System.nanoTime();
            while (threadsinwork >= threadnumber) {
                wait();
            }
            addWaitingTime(System.nanoTime() - start);
            executor.execute(process);
            ++threadsinwork;
            ++startedthreads;
        } catch (InterruptedException ex) {
            //TODO
            Logger.getLogger(AbstractProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void restartProcess(Process process) {
        executor.execute(process);
        ++restartedthreads;
    }

    private void processingSuccessfullyFinished(Collection<NGSRecord> records) {
        if (resultstorage != null) {
            resultstorage.addNGSRecordsCollection(records);
        }
        ++successfullthreads;
        successfullrecords += records.size();
        synchronized(this) {
            --threadsinwork;
            notify();
        }
    }
    
    private void processingCanNotBeFinished(Collection<NGSRecord> records, Exception ex) {
        if (failedstorage != null) {
            for (final NGSRecord record : records) {
                record.loqError(ex);
                failedstorage.addNGSRecord(record);
            }
        }
        ++failedthreads;
        failedrecords += records.size();
        synchronized(this) {
            --threadsinwork;
            notify();
        }
    }

    private void addWaitingTime(long t) {
        waitingsum -= (waiting[waitingcursor] - t);
        waiting[waitingcursor] = t;
        waitingcursor = (waitingcursor + 1) % waitingsize;
    }
    
    public void printReport() {
        System.out.print(processorname + "\t");
        System.out.print(threadsinwork + "/" + this.threadnumber + "\t");
        System.out.print(waitingsum/waitingsize/1000 + "\t\t");
        System.out.print(successfullrecords + "\t\t" + failedrecords + "\n");
    }
    
///////////
    
    protected abstract class Process implements Runnable {

        @Override
        public void run() {
            try {
                processing();
                processingSuccessfullyFinished(getRecords());
            } catch (NCBIConnectionException | DataBaseResponseException ex) {
                processingCanNotBeFinished(getRecords(), ex);
            } catch (BLASTException | ParseException | TaxonomyException | SQLException ex) {
                processingCanNotBeFinished(getRecords(), ex);
            } catch (Exception ex) {
                processingCanNotBeFinished(getRecords(), ex);
            } 
        }
        
        protected abstract void processing() 
                throws NCBIConnectionException, BLASTException, 
                ParseException, TaxonomyException, 
                DataBaseResponseException, SQLException;
        protected abstract Process cloneProcess();
        protected abstract Collection<NGSRecord> getRecords();
    }
    
}
