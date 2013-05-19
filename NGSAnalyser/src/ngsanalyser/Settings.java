package ngsanalyser;

import com.beust.jcommander.Parameter;

public class Settings {
    @Parameter(names = {"-e", "--experiment"}, description = "experiment information", required = false)
    public String experiment = "Franssen.xml";
    
    @Parameter(names = {"-r", "--run"}, description = "run of experiment", required = true)
    public String run;

    @Parameter(names= {"-s", "--ngsfile"}, description = "file reads", required = true)
    public String ngsfile = null;

    @Parameter(names= {"-u", "--url"}, description = "bd url", required = false)
    public String url = "193.218.141.85:1981";

    @Parameter(names= {"-l", "--login"}, description = "bd user", required = false)
    public String login = "alexander";

    @Parameter(names= {"-p", "--password"}, description = "bd password", required = false)
    public String password = "1981ujlfhj;ltybz";

    @Parameter(names= {"-t", "--taxonomy"}, description = "path to file with taxons", required = false)
    public String taxonomy = null;
}
