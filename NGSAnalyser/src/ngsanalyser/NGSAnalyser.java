package ngsanalyser;

import ngsanalyser.experiment.Experiment;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.sql.SQLException;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.BLASTException;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.NoDataBaseResponseException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSFile;
import ngsanalyser.ngsdata.NGSFileException;
import ngsanalyser.processor2.Processing;
import ngsanalyser.taxonomy.Taxonomy;
import ngsanalyser.taxonomy.TaxonomyException;

public class NGSAnalyser {

    public static void main(String[] args) throws NGSFileException, InterruptedException, TaxonomyException, ParseException, IOException, SQLException, NoConnectionException, NoDataBaseResponseException, BLASTException {
        final Settings settings = new Settings();
        new JCommander(settings, args);
        
        DBService.INSTANCE.setConnectionParametr(settings.url, settings.login, settings.password);
        Taxonomy.INSTANCE.loadData(settings.taxonomy);

        final Experiment experiment = Experiment.createInstance(settings.experiment);
        final Run run = experiment.getRun(settings.run);

        final NGSFile fastqfile = NGSFile.NGSFileFactory(settings.ngsfile);
        
        
        final Processing pr = new Processing(run, fastqfile);
        pr.startProcessing();

        //        pr.startMonitoring();
        
    }
}
