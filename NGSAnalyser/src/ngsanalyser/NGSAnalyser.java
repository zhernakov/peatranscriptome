package ngsanalyser;

import ngsanalyser.ngsdata.NGSRecordsCollection;
import com.beust.jcommander.JCommander;
import ngsanalyser.blaster.BLASTer;
import ngsanalyser.blasthitsanalyzer.BLASTHitsAnalyzer;
import ngsanalyser.ngsdata.NGSFile;
import ngsanalyser.ngsdata.NGSFileException;
import ngsanalyser.taxonomy.Taxonomy;
import ngsanalyser.taxonomy.TaxonomyException;

public class NGSAnalyser {

    public static void main(String[] args) throws NGSFileException, InterruptedException, TaxonomyException {
        final Settings settings = new Settings();
        new JCommander(settings, args);
        
        System.out.println(settings.infofile);
        System.out.println(settings.ngsfile);
        
        final NGSFile fastqfile = NGSFile.NGSFileFactory(settings.ngsfile);
        
        final NGSRecordsCollection failedstorage = new NGSRecordsCollection();
        final NGSRecordsCollection blaststorage = new NGSRecordsCollection();
//        final NGSRecordsCollection parsingstorage = new NGSRecordsCollection();
        final NGSRecordsCollection analysisstorage = new NGSRecordsCollection();
        
        final Taxonomy taxonomy = Taxonomy.getDefaultInstance();
        
        final BLASTer blaster = new BLASTer(fastqfile, blaststorage, failedstorage, 14);
//        final BLASTResultParser parser = new BLASTResultParser(blaststorage, parsingstorage, failedstorage);
        final BLASTHitsAnalyzer analyzer = new BLASTHitsAnalyzer(blaststorage, analysisstorage, failedstorage, 20, taxonomy);
        
        blaster.startBLAST();
//        parser.startParsing();
        analyzer.startAnalysis();
        
        System.out.println(failedstorage.getNumber());
        
//        final Processor processor = new Processor(fastqfile);
//        processor.startAnalysis();

    }
}
