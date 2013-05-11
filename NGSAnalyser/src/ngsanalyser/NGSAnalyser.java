package ngsanalyser;

import ngsanalyser.experiment.Experiment;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.sql.SQLException;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.NoConnectionException;
import ngsanalyser.exception.NoDataBaseRespondException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.experiment.Run;
import ngsanalyser.ngsdata.NGSFile;
import ngsanalyser.ngsdata.NGSFileException;
import ngsanalyser.processor.Processing;
import ngsanalyser.taxonomy.Taxonomy;
import ngsanalyser.taxonomy.TaxonomyException;

public class NGSAnalyser {

    public static void main(String[] args) throws NGSFileException, InterruptedException, TaxonomyException, ParseException, IOException, SQLException, NoConnectionException, NoDataBaseRespondException {
        final Settings settings = new Settings();
        new JCommander(settings, args);
        
        DBService.INSTANCE.setConnectionParametr(settings.url, settings.login, settings.password);

        Taxonomy.loadSource();

        final Experiment experiment = Experiment.createInstance(settings.experiment);
        final Run run = experiment.getRun(settings.run);

        final NGSFile fastqfile = NGSFile.NGSFileFactory(settings.ngsfile);

        Processing pr = new Processing(run, fastqfile);
        pr.startProcessing();
        
//        final NGSFile fastqfile = NGSFile.NGSFileFactory(settings.ngsfile);
//        final NGSRecordsCollection blaststorage = new NGSRecordsCollection();
//        final NGSRecordsCollection failedstorage = new NGSRecordsCollection();
//        
//        final BLASTer blaster = new BLASTer(fastqfile, blaststorage, failedstorage, run, 20);
//
//        final DataBaseStorager storager = new DataBaseStorager(failedstorage, run);
//        
//        final HitsAnalyzer analyzer = new HitsAnalyzer(blaststorage, storager, failedstorage, 15);
//        
//        blaster.startBLAST();
//        analyzer.startAnalysis();
        
    }
}
