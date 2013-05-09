package ngsanalyser.experiment;

import java.sql.SQLException;
import java.util.List;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.ngsdata.NGSRecord;

public class Run {
    private final String title;
    private final String description;
    private final int species;
    private final String breed;
    private final String source;
    private final String platform;
    
    private final String rundbid;

    public Run(
            String expdbid, String secretid, String title, String description,
            int species, String breed, String source, String platform) throws SQLException {
        this.title = title;
        this.description = description;
        this.species = species;
        this.breed = breed;
        this.source = source;
        this.platform = platform;
        this.rundbid = getRunDbId(expdbid, secretid);
    }

    private String getRunDbId(String expdbid, String secretid) throws SQLException {
        String dbid = DBService.INSTANCE.getRunId(expdbid, secretid, title);
        if (dbid == null) {
            dbid = DBService.INSTANCE.addRun(expdbid, secretid, title, description, species, breed, source, platform);
        }
        return dbid;
    }

    public String getTitle() {
        return title;
    }

    private int getDataBaseId() {
        return 1;
    }
    public StringBuilder composeSequencesInsertQuery(List<NGSRecord> records) {
        final StringBuilder builder = new StringBuilder();
        
        builder.append("INSERT INTO sequences ("
                + "runid, readid, additional, sequence, quality, length, taxid"
                + ") VALUES\n");
        
        for (final NGSRecord record : records) {
            builder.append("(")
                    .append(rundbid)
                    .append(",")
                    .append(record.composeDBInsertValues())
                    .append("),\n");
        }
        
        int length =builder.length();
        builder.deleteCharAt(builder.length() - 2);
        
        return builder;
    }

}
