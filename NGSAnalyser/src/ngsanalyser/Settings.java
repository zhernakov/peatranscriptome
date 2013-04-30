package ngsanalyser;

import com.beust.jcommander.Parameter;

public class Settings {
    @Parameter(names = {"-i", "--infofile"}, description = "analysis information", required = true)
    public String infofile;
    
    @Parameter(names= {"-s", "--ngsfile"}, description = "file reads", required = true)
    public String ngsfile;
}
