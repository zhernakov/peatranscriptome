package ngsanalyser;

import ngsanalyser.ngsdata.NGSRecordsCollection;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import ngsanalyser.exception.ParsingException;
import ngsanalyser.processes.blaster.BLASTer;
import ngsanalyser.processes.hitsanalyzer.HitsAnalyzer;
import ngsanalyser.ngsdata.NGSFile;
import ngsanalyser.ngsdata.NGSFileException;
import ngsanalyser.taxonomy.Taxonomy;
import ngsanalyser.taxonomy.TaxonomyException;

public class NGSAnalyser {

    public static void main(String[] args) throws NGSFileException, InterruptedException, TaxonomyException, ParsingException, IOException {
        final Settings settings = new Settings();
        new JCommander(settings, args);
        
        System.out.println(settings.experiment);
        System.out.println(settings.ngsfile);
        
        Experiment info = Experiment.createInstance(settings.experiment);
        
        int c = 0;
        
//        Taxonomy.loadSource();
//        
//        final NGSFile fastqfile = NGSFile.NGSFileFactory(settings.ngsfile);
//        
//        final NGSRecordsCollection failedstorage = new NGSRecordsCollection();
//        final NGSRecordsCollection blaststorage = new NGSRecordsCollection();
//        final NGSRecordsCollection analysisstorage = new NGSRecordsCollection();
//        
//        final BLASTer blaster = new BLASTer(fastqfile, blaststorage, failedstorage, 14);
//        final HitsAnalyzer analyzer = new HitsAnalyzer(blaststorage, analysisstorage, failedstorage, 20);
//        
//        blaster.startBLAST();
//        analyzer.startAnalysis();
        
    }
}
