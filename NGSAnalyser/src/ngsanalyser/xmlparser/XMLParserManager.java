package ngsanalyser.xmlparser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.blaster.BLASTManager;
import ngsanalyser.ngsdata.NGSAddible;
import ngsanalyser.ngsdata.NGSRecord;

public class XMLParserManager {
    private final int threadnumber;
    private int threadinwork = 0;
    private final NGSAddible resultstorage;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    public XMLParserManager(int threadnumber, NGSAddible resultstorage) {
        this.threadnumber = threadnumber;
        this.resultstorage = resultstorage;
    }
    
    synchronized public void startParsing(NGSRecord record) {
        try {
            while (threadinwork >= threadnumber) {
                wait();
            }
            final XMLParsing process = new XMLParsing(this, record);
            executor.execute(process);
            ++threadinwork;
        } catch (InterruptedException ex) {
            Logger.getLogger(XMLParserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    synchronized public void finishParsing(NGSRecord record) {
        resultstorage.addNGSRecord(record);
        if (--threadinwork == 0 && executor.isShutdown()) {
            resultstorage.terminate();
            System.out.println(resultstorage.getNumber() + " records were added to stotage after Parsing");
        }
        notify();
    }

    synchronized public void shutdown() {
        executor.shutdown();
    }
}
