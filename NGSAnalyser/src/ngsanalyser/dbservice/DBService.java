package ngsanalyser.dbservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSRecord;

public class DBService {
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
    
    private synchronized ResultSet executeQuery(String query) throws SQLException {
        if (connection == null) {
            connect();
        }
        final Statement statement = connection.createStatement();
        
        return statement.executeQuery(query);
    } 
  
    private synchronized boolean execute(String query) throws SQLException {
        if (connection == null) {
            connect();
        }
        final Statement statement = connection.createStatement();
        return statement.execute(query);
    }

    private PreparedStatement getPreparedStatement(String template) throws SQLException {
        if (connection == null) {
            connect();
        }
        return connection.prepareStatement(template);
    }

    public String getExperimentId(String secretid, String title) throws SQLException {
        final String query = "SELECT id FROM experiments WHERE "
                + "secretid='" + secretid + "' AND "
                + "title='" + title + "';";
        
        final ResultSet result = executeQuery(query);
        if (result.next()) {
            return result.getString("id");
        } else {
            return null;
        }
    }

    public String addExperiment(String secretid, String title, String description) throws SQLException {
        final String statement = "INSERT INTO experiments (secretid, title, description) VALUES ('"
                + secretid + "', '"
                + title + "', '"
                + description + "');";
        System.out.println(statement);
        execute(statement);
        return getExperimentId(secretid, title);
    }

    public String getRunId(String expdbid, String secretid, String title) throws SQLException {
        final String query = "SELECT id FROM runs WHERE "
                + "experimentid=" + expdbid + " AND "
                + "secretid='" + secretid + "' AND "
                + "title='" + title + "';";
        
        final ResultSet result = executeQuery(query);
        if (result.next()) {
            return result.getString("id");
        } else {
            return null;
        }
    }

    public String addRun(
            String expdbid, String secretid, String title, String description,
            int species, String breed, String source, String platform) throws SQLException {
        final String statement = "INSERT INTO runs ("
                + "experimentid, secretid, title, description, species, breed, source, platform"
                + ") VALUES ("
                + expdbid + ", '"
                + secretid + "', '"
                + title + "', '"
                + description + "', "
                + species + ", '"
                + breed + "', '"
                + source + "', '"
                + platform + "');";
        System.out.println(statement);
        execute(statement);
        return getRunId(expdbid, secretid, title);
    }
  
    public void addSequences(Run run, List<NGSRecord> records) throws SQLException {
        final String template = "INSERT INTO sequences "
                + "(runid, readid, additional, sequence, quality, length, taxid) "
                + "VALUES (" + run.rundbid + ", ?, ?, ?, ?, ?, ?)";
        final PreparedStatement statement = getPreparedStatement(template);
       
        for (final NGSRecord record : records) {
            statement.setString(1, record.recordid);
            statement.setString(2, record.additionalinfo);
            statement.setString(3, record.sequence);
            statement.setString(4, record.quality);
            statement.setString(5, Integer.toString(record.length));
            statement.setString(6, Integer.toString(record.getTaxonId()));
            statement.addBatch();
        }
        statement.executeBatch();
    }
}
