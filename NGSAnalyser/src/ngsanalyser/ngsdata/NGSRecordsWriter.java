package ngsanalyser.ngsdata;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public class NGSRecordsWriter implements NGSAddible {
    private final String name;
    private PrintWriter  writer;
    
    public NGSRecordsWriter(String name) {
        this.name = name;
    }
    
    @Override
    public void addNGSRecord(NGSRecord record) {
        try {
            if (writer == null) {
                writer = new PrintWriter (new File(name));
            }
            writer.write(record.recordid);
            writer.write("\r\n");
            writer.write(record.additionalinfo);
            writer.write("\r\n");
            writer.write(record.sequence);
            writer.write("\r\n");
            writer.write(record.quality);
            writer.write("\r\n{");
            writer.write(record.getExceptionMessage());
            writer.write("}\r\n");
            writer.flush();
        } catch (IOException ex) {
            System.err.println("Can't write failed records to file");
        }
    }

    @Override
    public void addNGSRecordsCollection(Collection<NGSRecord> records) {
        for (final NGSRecord record : records) {
            addNGSRecord(record);
        }
    }

    @Override
    public void terminate() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

    @Override
    public int getNumber() {
        return 0;
    }
    
}
