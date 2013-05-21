package ngsanalyser.ncbiservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngsanalyser.exception.BLASTException;
import ngsanalyser.exception.NCBIConnectionException;
import ngsanalyser.exception.ParseException;
import ngsanalyser.ngsdata.NGSRecord;

public class NCBIService {
    public static final NCBIService INSTANCE = new NCBIService();
    private static final Timer timer = new Timer(3000);
    
    private static final String blastlink = "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi";
    private static final String eutilslink = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi";
    private static final int waitinterval = 30000;
    
    private NCBIService() {
    }
    
    public static void printStream(InputStream is) {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException ex) {
        }
    }
    
    //////////

    private URLConnection send(String statement) throws NCBIConnectionException {
        final int attempts = 10;
        for (int attemp = 0; attemp < attempts; ++attemp) {
            try {
                final URL url = new URL(statement);
                timer.getPermission();
                final URLConnection connection = url.openConnection();
                connection.setConnectTimeout(10000);
                return connection;
            } catch (IOException ex) {
                try {
                    Thread.sleep(waitinterval);
                } catch (InterruptedException ex1) {

                }
            }
        }
        throw new NCBIConnectionException("");
    }
    
    private InputStream sendQuery(String statement) throws NCBIConnectionException {
        try {
            return send(statement).getInputStream();
        } catch (IOException ex) {
            throw new NCBIConnectionException(ex);
        }
    }  

    //---BLAST---//
    
    public InputStream multiMegaBlast(Collection<NGSRecord> records) throws BLASTException, ParseException, NCBIConnectionException {
        final String queryId = sendBLASTQuery(records);
        waitBLASTResults(queryId);
        final InputStream stream = getBLASTResults(queryId);
        sendBLASTDeleteRequest(queryId);
        return stream;
    }
    
    private String sendBLASTQuery(Collection<NGSRecord> records) throws BLASTException, NCBIConnectionException {
        final String statement = composeBLASTStatement(records);
        System.out.println(statement);
        final InputStream in = sendQuery(statement);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                System.out.print(line);
                if (!line.contains("class=\"error\"") && !line.contains("Message ID#")) {
                    if (line.contains("RID = ")) {
                        reader.close();
                        return line.split("=")[1].trim();
                    }
                } else {
                    final String cause = line.split("</p></li></ul>")[0].split("<p class=\"error\">")[1].trim();
                    throw new BLASTException("NCBI QBlast refused this request because: " + cause);
                }
            }
        } catch (IOException ex) {
            throw new NCBIConnectionException(ex);
        }
        throw new BLASTException("Unable to retrieve request ID");
    }

    private String composeBLASTStatement(Collection<NGSRecord> records) {
        final StringBuilder builder = new StringBuilder();
        builder.append(blastlink)
                .append("?CMD=Put&PROGRAM=blastn&MEGABLAST=on&DATABASE=nr&QUERY=");
        for (final NGSRecord record : records) {
            builder.append("%3E")
                    .append(record.recordid)
                    .append("%0D%0A")
                    .append(record.sequence)
                    .append("%0D%0A");
        }
        return builder.toString();
    }

    private void waitBLASTResults(String queryId) throws BLASTException, NCBIConnectionException {
        final String statement = blastlink + "?CMD=Get&RID=" + queryId;
//        System.out.println(statement);
        
        while (true) {
            final InputStream in = sendQuery(statement);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.contains("READY")) {
                        reader.close();
                        return;
                    } else if (line.contains("WAITING")) {
                        try {
                            Thread.sleep(this.waitinterval);
                        } catch (InterruptedException ex) {
                            //TODO
                            Logger.getLogger(NCBIService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        reader.close();
                        break;
                    } else if (line.contains("UNKNOWN")) {
                        reader.close();
                        throw new BLASTException("Unknown request id - no results exist for it. Given id = " + queryId);
                    }
                }
            } catch (IOException ex) {
                throw new NCBIConnectionException(ex);
            }
        }
    }

    private InputStream getBLASTResults(String queryId) throws BLASTException, NCBIConnectionException {
        final String statement = blastlink + "?CMD=Get&FORMAT_TYPE=XML&RID=" + queryId;
        return sendQuery(statement);
    }
    
    private void sendBLASTDeleteRequest(String queryId) throws NCBIConnectionException {
        final String statement = blastlink + "?CMD=Delete&RID=" + queryId;
//        System.out.println(statement);
        send(statement);
    }

    //---ELINK---//
    public InputStream defineTaxonsSet(Iterable<String> ids) throws NCBIConnectionException {
        final String url = composeGetTaxonsSetStatement(ids);
        return sendQuery(url);
    }
        
    private String composeGetTaxonsSetStatement(Iterable<String> ids) {
        final StringBuilder builder = new StringBuilder();
        builder.append(eutilslink)
                .append("?dbfrom=nucleotide&db=taxonomy&id=");
        for (final String id : ids) {
            builder.append(id).append(",");
        }
        return builder.substring(0, builder.length() - 1);
    }
}
