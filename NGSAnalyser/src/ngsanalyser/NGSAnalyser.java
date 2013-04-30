package ngsanalyser;

import com.beust.jcommander.JCommander;
import java.io.IOException;
import ngsanalyser.ngsdata.NGSFile;
import ngsanalyser.ngsdata.exception.NGSFileException;

public class NGSAnalyser {

    public static void main(String[] args) throws NGSFileException {
        final Settings settings = new Settings();
        new JCommander(settings, args);
        
        System.out.println(settings.infofile);
        System.out.println(settings.ngsfile);
        
        final NGSFile fastqfile = NGSFile.NGSFileFactory(settings.ngsfile);

    }
}
