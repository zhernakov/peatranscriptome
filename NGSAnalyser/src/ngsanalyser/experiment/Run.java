package ngsanalyser.experiment;

import java.sql.SQLException;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.DataBaseResponseException;

public class Run {
    private final String title;
    private final String description;
    private final int species;
    private final String breed;
    private final String source;
    private final String platform;
    
    public final int db_runid;

    public Run(
            int expdbid, String secretid, String title, String description,
            int species, String breed, String source, String platform) throws SQLException, DataBaseResponseException {
        this.title = title;
        this.description = description;
        this.species = species;
        this.breed = breed;
        this.source = source;
        this.platform = platform;
        this.db_runid = getRunDbId(expdbid, secretid);
    }

    private int getRunDbId(int expdbid, String secretid) throws SQLException, DataBaseResponseException {
        int dbid = DBService.INSTANCE.getRunId(expdbid, secretid, title);
        if (dbid == -1) {
            dbid = DBService.INSTANCE.addRun(expdbid, secretid, title, description, species, breed, source, platform);
        }
        return dbid;
    }
}
