package ngsanalyser;

import ngsanalyser.experiment.Experiment;
import ngsanalyser.ngsdata.NGSRecordsCollection;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.sql.SQLException;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.ParsingException;
import ngsanalyser.experiment.Run;
import ngsanalyser.processes.blaster.BLASTer;
import ngsanalyser.processes.hitsanalyzer.HitsAnalyzer;
import ngsanalyser.ngsdata.NGSFile;
import ngsanalyser.ngsdata.NGSFileException;
import ngsanalyser.processes.databasestorager.DataBaseStorager;
import ngsanalyser.taxonomy.Taxonomy;
import ngsanalyser.taxonomy.TaxonomyException;

public class NGSAnalyser {

    public static void main(String[] args) throws NGSFileException, InterruptedException, TaxonomyException, ParsingException, IOException, SQLException, NoConnectionException {
        final Settings settings = new Settings();
        new JCommander(settings, args);
        
        DBService.INSTANCE.setConnectionParametr(settings.url, settings.login, settings.password);

        final Experiment experiment = Experiment.createInstance(settings.experiment);
        final Run run = experiment.getRun(settings.run);
        
        
        Taxonomy.loadSource();
 
        
        final NGSFile fastqfile = NGSFile.NGSFileFactory(settings.ngsfile);
        final NGSRecordsCollection blaststorage = new NGSRecordsCollection();
        final NGSRecordsCollection failedstorage = new NGSRecordsCollection();
        
        final BLASTer blaster = new BLASTer(fastqfile, blaststorage, failedstorage, 14);

        final DataBaseStorager storager = new DataBaseStorager(failedstorage, run);
        
        final HitsAnalyzer analyzer = new HitsAnalyzer(blaststorage, storager, failedstorage, 20);
        
        blaster.startBLAST();
        analyzer.startAnalysis();
        
    }
}
