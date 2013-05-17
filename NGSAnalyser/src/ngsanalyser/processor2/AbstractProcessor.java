package ngsanalyser.processor2;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.exception.BLASTException;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.NoDataBaseResponseException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;
import ngsanalyser.taxonomy.TaxonomyException;

public abstract class AbstractProcessor implements NGSAddible {
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

    protected final NGSAddible resultstorage;
    protected final NGSAddible failedstorage;

    public AbstractProcessor(NGSAddible resultstorage, NGSAddible failedstorage, int threadnumber) {
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
    public synchronized void terminate() {
        while (threadsinwork != 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                //TODO
                Logger.getLogger(ngsanalyser.processor.AbstractProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        executor.shutdown();
        if (failedstorage != null) {
            failedstorage.terminate();
        }
        if (resultstorage != null) {
            resultstorage.terminate();
        }
        System.out.println("Processor is terminated:\n"
                + "\tstarted threads:" + startedthreads
                + "\tsuccessfull threads:" + successfullthreads 
                + "treated records: " + successfullrecords);
    }

    @Override
    public int getNumber() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            Logger.getLogger(ngsanalyser.processor.AbstractProcessor.class.getName()).log(Level.SEVERE, null, ex);
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
        synchronized(this) {
            --threadsinwork;
            notify();
        }
    }
    
    private void processingCanNotBeFinished(Collection<NGSRecord> records) {
        if (failedstorage != null) {
            failedstorage.addNGSRecordsCollection(records);
        }
        ++failedthreads;
        synchronized(this) {
            --threadsinwork;
            notify();
        }
    }

    private void addWaitingTime(long t) {
        waiting[waitingcursor] = t;
        waitingcursor = (waitingcursor + 1) % waitingsize;
    }
    
///////////
    
    protected abstract class Process implements Runnable {

        @Override
        public void run() {
            try {
                processing();
                processingSuccessfullyFinished(getRecords());
            } catch (NoConnectionException ex) {
                restartProcess(cloneProcess());
            } catch (BLASTException | ParseException | TaxonomyException ex) {
                processingCanNotBeFinished(getRecords());
            } catch (SQLException ex) {
                Logger.getLogger(AbstractProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoDataBaseResponseException ex) {
                Logger.getLogger(AbstractProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        protected abstract void processing() throws NoConnectionException, BLASTException, ParseException, TaxonomyException, SQLException, NoDataBaseResponseException ;
        protected abstract Process cloneProcess();
        protected abstract Collection<NGSRecord> getRecords();
    }
    
}
