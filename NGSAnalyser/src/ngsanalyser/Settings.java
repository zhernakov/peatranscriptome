package ngsanalyser;

import com.beust.jcommander.Parameter;

public class Settings {
    @Parameter(names = {"-e", "--experiment"}, description = "experiment information", required = true)
    public String experiment;
    
    @Parameter(names= {"-s", "--ngsfile"}, description = "file reads", required = true)
    public String ngsfile;

    @Parameter(names= {"-h", "--host"}, description = "bd host", required = false)
    public String host;
}
