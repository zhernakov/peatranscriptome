package ngsanalyser.dbservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import ngsanalyser.Experiment;
import ngsanalyser.exception.NoConnectionException;

public class DBService {
    public final static DBService INSTANCE = new DBService();
    
    private Connection connection;
    
    private String url;
    private String user;
    private String password;
    
    private final String databasename = "peatranscriptome";
    
    private Experiment experiment;

    private DBService() {
        
    }
    
    public void setConnectionParametr(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }
    
    public void setExperiment(Experiment experiment) throws SQLException, NoConnectionException {
        this.experiment = experiment;
        connect();
        String id = getExperimentId();
        if (id == null) {
            addExperiment();
            id = getExperimentId();
        }
        System.out.println(id);
    }
    
    public void connect() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:mysql://" + url + "/" + databasename, user, password);
            connection.setSchema(databasename);
            getExperimentId();
        }
    }
    
    private String getExperimentId() throws SQLException{
        final String query = "SELECT id FROM experiments WHERE title=\"" 
                + experiment.getTitle() + "\";";
        final Statement statement = connection.createStatement();
        final ResultSet result = statement.executeQuery(query);
        if (result.next()) {
            return result.getString("id");
        } else {
            return null;
        }
    }
    
    private void addExperiment() throws SQLException, NoConnectionException {
        final String query = "INSERT INTO experiments (title, description) VALUES ('" +
                experiment.getTitle() + "','" + experiment.getDescription() + "');";
        System.out.println(query);
        final Statement statement = connection.createStatement();
        boolean executed = statement.execute(query);
        if (executed) {
            throw new NoConnectionException("");
        }
    }

    public void sendInsertQuery(StringBuilder query) throws SQLException, NoConnectionException {
        System.out.println(query);
    }
}
