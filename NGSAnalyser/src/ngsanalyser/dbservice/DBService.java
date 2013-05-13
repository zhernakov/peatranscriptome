package ngsanalyser.dbservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import ngsanalyser.exception.NoDataBaseResponseException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSRecord;

public class DBService {
    private static int getId(ResultSet result) throws SQLException {
        if (result.next()) {
            return result.getInt("id");
        } else {
            return -1;
        }
    }

    public final static DBService INSTANCE = new DBService();
    
    private final String databasename = "peatranscriptome";

    private Connection connection;
    
    private String url;
    private String user;
    private String password;
    
    private DBService() {
        
    }
    
    public void setConnectionParametr(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }
    
    private void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://" + url + "/" + databasename, user, password);
    }

    private PreparedStatement getPreparedStatement(String template) throws SQLException, NoDataBaseResponseException {
        try {
            if (connection == null) {
                connect();
            }
            return connection.prepareStatement(template);
        } catch (SQLException ex) {
            int errorcode = ex.getErrorCode();
            if (errorcode < 2058 && errorcode > 1999) {
                throw new NoDataBaseResponseException();
            } else {
                throw ex;
            }
        }
    }

    public int getExperimentId(String secretid, String title) throws SQLException, NoDataBaseResponseException {
        final String template = "SELECT id FROM experiments WHERE "
                + "secretid = ? AND title = ?;";
        final PreparedStatement statement = getPreparedStatement(template);
        statement.setString(1, secretid);
        statement.setString(2, title);

        return getId(statement.executeQuery());
    }

    public int addExperiment(String secretid, String title, String description) throws SQLException, NoDataBaseResponseException {
        final String template = "INSERT INTO experiments (secretid, title, description) "
                + "VALUES (?, ?, ?);";
        final PreparedStatement statement = getPreparedStatement(template);
        statement.setString(1, secretid);
        statement.setString(2, title);
        statement.setString(3, description);
        
        statement.executeUpdate();
        return getExperimentId(secretid, title);
    }

    public int getRunId(int expdbid, String secretid, String title) throws SQLException, NoDataBaseResponseException {
        final String template = "SELECT id FROM runs WHERE "
                + "experimentid = ? AND secretid = ? AND title = ?;";
        final PreparedStatement statement = getPreparedStatement(template);
        statement.setInt(1, expdbid);
        statement.setString(2, secretid);
        statement.setString(3, title);

        return getId(statement.executeQuery());
    }

    public int addRun(
            int expdbid, String secretid, String title, String description,
            int species, String breed, String source, String platform) throws SQLException, NoDataBaseResponseException {
        final String template = "INSERT INTO runs ("
                + "experimentid, secretid, title, description, species, breed, source, platform"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        final PreparedStatement statement = getPreparedStatement(template);
        statement.setInt(1, expdbid);
        statement.setString(2, secretid);
        statement.setString(3, title);
        statement.setString(4, description);
        statement.setInt(5, species);
        statement.setString(6, breed);
        statement.setString(7, source);
        statement.setString(8, platform);

        statement.executeUpdate();
        return getRunId(expdbid, secretid, title);
    }
  
    public void addSequences(Run run, List<NGSRecord> records) throws SQLException, NoDataBaseResponseException {
        final String template = "INSERT INTO sequences "
                + "(runid, readid, additional, sequence, quality, length, taxid) "
                + "VALUES (" + run.db_runid + ", ?, ?, ?, ?, ?, ?)";
        final PreparedStatement statement = getPreparedStatement(template);
       
        for (final NGSRecord record : records) {
            statement.setString(1, record.recordid);
            statement.setString(2, record.additionalinfo);
            statement.setString(3, record.sequence);
            statement.setString(4, record.quality);
            statement.setInt(5, record.length);
            statement.setInt(6, record.getTaxonId());
            statement.addBatch();
        }
        statement.executeBatch();
    }

    public Set<String> getStoragedSequences(Run run) throws NoDataBaseResponseException, SQLException {
        final Set<String> set = new TreeSet<>();
        final String template = "SELECT readid FROM sequences WHERE runid = ?;";
        final PreparedStatement statement = getPreparedStatement(template);
        statement.setInt(1, run.db_runid);
        final ResultSet result = statement.executeQuery();

        while (result.next()) {
            set.add(result.getString("readid"));
        }
        return set;
    }
}
