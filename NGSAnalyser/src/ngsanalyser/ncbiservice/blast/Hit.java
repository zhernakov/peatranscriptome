package ngsanalyser.ncbiservice.blast;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Hit {
    private Map<DBID,List<String>> identifiers = new HashMap<>();
    private String defenition;
    private String accession;
    private List<Hsp> hsps;

    public void setIdentifiers(String string) {
        DBID dbid = null;
        for (final String element : string.split("\\|")) {
            switch (element) {
                case "gb":
                    dbid = DBID.gb;
                    identifiers.put(dbid, new LinkedList<String>());
                    break;
                case "gi":
                    dbid = DBID.gi;
                    identifiers.put(dbid, new LinkedList<String>());
                    break;
                default:
                    if (dbid != null) {
                        identifiers.get(dbid).add(element);
                    }
                    break;
            }
        }
    }

    public void setDefenition(String Hit_def) {
        this.defenition = Hit_def;
    }

    public void setAccession(String Hit_accession) {
        this.accession = Hit_accession;
    }

    public void addHsp(Hsp hsp) {
        if (hsps == null) {
            hsps = new LinkedList<>();
        }
        hsps.add(hsp);
    }

    public String getDefenition() {
        return defenition;
    }

    public String getAccession() {
        return accession;
    }

    double getMinimalEValue() {
        double minevalue = 1;
        for (final Hsp hsp : hsps) {
            final double hspevalue = hsp.getEValue();
            if (hspevalue < minevalue) {
                minevalue = hspevalue;
            }
        }
        return minevalue;
    }

    Collection<? extends String> getSeqIds(DBID dbid) {
        return identifiers.get(dbid);
    }
}
