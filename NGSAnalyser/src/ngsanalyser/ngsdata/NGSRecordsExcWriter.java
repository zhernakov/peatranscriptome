package ngsanalyser.ngsdata;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public class NGSRecordsExcWriter implements NGSAddibleExc {
    private final String name;
    private PrintWriter  fastqwriter;
    private PrintWriter  excwriter;

    public NGSRecordsExcWriter(String name) {
        this.name = name;
    }

    @Override
    public synchronized void addNGSRecord(NGSRecord record, Exception exc) {
        writeFastq(record);
        writeRecordId(record.recordid);
        writeException(exc);
    }

    @Override
    public synchronized void addNGSRecordsCollection(Collection<NGSRecord> records, Exception exc) {
        for (final NGSRecord record : records){
            writeFastq(record);
            writeRecordId(record.recordid);
        }
        writeException(exc);
    }
    
    private void writeFastq(NGSRecord record) {
        try {
            if (fastqwriter == null) {
                fastqwriter = new PrintWriter (new File("failed_" + name + ".fastq"));
            }
            fastqwriter.write("@");
            fastqwriter.write(record.recordid);
            fastqwriter.write(" ");
            fastqwriter.write(record.additionalinfo);
            fastqwriter.write("\r\n");
            fastqwriter.write(record.sequence);
            fastqwriter.write("\r\n+\r\n");
            fastqwriter.write(record.quality);
            fastqwriter.write("\r\n");
            fastqwriter.flush();
        } catch (IOException ex) {
            System.err.println("Can't write failed records to file");
        }
    }
    
    private void writeRecordId(String id) {
        try {
            if (excwriter == null) {
                excwriter = new PrintWriter (new File("failed_" + name + "report.txt"));
            }
            excwriter.write(id);
            excwriter.write(", ");
        } catch (IOException ex) {
            System.err.println("Can't write failed records to file");
        }
    }
    
    private void writeException(Exception exc) {
        try {
            if (excwriter == null) {
                excwriter = new PrintWriter (new File("failed_" + name + "report.txt"));
            }
            excwriter.write("\r\n{");
            try {
                excwriter.write(exc.getMessage());
            } catch (Exception ex) {
                excwriter.write("unknown");
            }
            excwriter.write("}\r\n");
            excwriter.flush();
        } catch (IOException ex) {
            System.err.println("Can't write failed records to file");
        }
    }

    @Override
    public void terminate() {
        if (fastqwriter != null) {
            fastqwriter.flush();
            fastqwriter.close();
        }
        if (excwriter != null) {
            excwriter.flush();
            excwriter.close();
        }
    }
}
