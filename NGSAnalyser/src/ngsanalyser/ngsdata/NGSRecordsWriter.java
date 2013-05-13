package ngsanalyser.ngsdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class NGSRecordsWriter implements NGSAddible {
    private final String name;
    private BufferedWriter writer;
    
    public NGSRecordsWriter(String name) {
        this.name = name;
    }
    
    @Override
    public void addNGSRecord(NGSRecord record) {
        try {
            if (writer == null) {
                writer = new BufferedWriter(new FileWriter(new File(name)));
            }
            writer.write(record.recordid);
            writer.write("\n");
            writer.write(record.additionalinfo);
            writer.write("\n");
            writer.write(record.sequence);
            writer.write("\n");
            writer.write(record.quality);
            writer.write("\n{");
            writer.write(record.getExceptionMessage());
            writer.write("}\n");
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
            try {
                writer.close();
            } catch (IOException ex) {
                System.err.println("Some of failed records can be lost in file");
            }
        }
    }

    @Override
    public int getNumber() {
        return 0;
    }
    
}
