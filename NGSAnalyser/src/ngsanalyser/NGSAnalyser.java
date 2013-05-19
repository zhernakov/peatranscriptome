package ngsanalyser;

import ngsanalyser.processing.BlastAnalyseStorage;
import ngsanalyser.experiment.Experiment;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.sql.SQLException;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.BLASTException;
import ngsanalyser.exception.NCBIConnectionException;
import ngsanalyser.exception.DataBaseResponseException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSFile;
import ngsanalyser.ngsdata.NGSFileException;
import ngsanalyser.taxonomy.Taxonomy;
import ngsanalyser.taxonomy.TaxonomyException;

public class NGSAnalyser {

    public static void main(String[] args) throws NGSFileException, InterruptedException, TaxonomyException, ParseException, IOException, SQLException, NCBIConnectionException, DataBaseResponseException, BLASTException {
        final Settings settings = new Settings();
        new JCommander(settings, args);
        
        DBService.INSTANCE.setConnectionParametr(settings.url, settings.login, settings.password);
        Taxonomy.INSTANCE.loadData(settings.taxonomy);

        final Experiment experiment = Experiment.createInstance(settings.experiment);
        final Run run = experiment.getRun(settings.run);

        final NGSFile fastqfile = NGSFile.NGSFileFactory(settings.ngsfile);
        
        
        final BlastAnalyseStorage pr = new BlastAnalyseStorage(run, fastqfile);
        pr.startProcessing();
        pr.startMonitoring();
    }
}
