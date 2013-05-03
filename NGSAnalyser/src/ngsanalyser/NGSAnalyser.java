package ngsanalyser;

import ngsanalyser.ngsdata.NGSRecordsCollection;
import com.beust.jcommander.JCommander;
import ngsanalyser.blaster.BLASTer;
import ngsanalyser.ngsdata.NGSFile;
import ngsanalyser.ngsdata.NGSFileException;
import ngsanalyser.xmlparser.XMLParser;

public class NGSAnalyser {

    public static void main(String[] args) throws NGSFileException, InterruptedException {
        final Settings settings = new Settings();
        new JCommander(settings, args);
        
        System.out.println(settings.infofile);
        System.out.println(settings.ngsfile);
        
        final NGSFile fastqfile = NGSFile.NGSFileFactory(settings.ngsfile);
        
        final NGSRecordsCollection blaststorage = new NGSRecordsCollection();
        final NGSRecordsCollection parsingstorage = new NGSRecordsCollection();
        
        final BLASTer blaster = new BLASTer(fastqfile, blaststorage, 14);
        final XMLParser parser = new XMLParser(blaststorage, parsingstorage);
        
        blaster.startBLAST();
        parser.startParsing();
        
        System.out.println(parsingstorage.getNumber());
        
//        final Processor processor = new Processor(fastqfile);
//        processor.startAnalysis();

    }
}
