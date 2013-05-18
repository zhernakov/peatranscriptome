package ngsanalyser.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import ngsanalyser.dbservice.DBService;
import ngsanalyser.exception.DataBaseResponseException;
import ngsanalyser.exception.ParseException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Experiment {
    public static Experiment createInstance(String path) throws FileNotFoundException, IOException, ParseException {
        try {
            final SAXParserFactory parserfactory = SAXParserFactory.newInstance();
            final SAXParser parser = parserfactory.newSAXParser();
            final ExperimentXMLHandler handler = new ExperimentXMLHandler();
            final InputStream is = new FileInputStream(new File(path));
            parser.parse(is, handler);
            return handler.experiment;
        } catch (ParserConfigurationException | SAXException ex) {
            throw new ParseException("Experiment file is corupted or data base does not respond", ex, false, true);
        }
    }

    public final String title;
    public final String description;
    private final Map<String,Run> runs = new TreeMap<>();
    private final List<String> publications = new LinkedList<>();

    public final int expdbid;
    
    private Experiment(String secretid, String title, String description) throws SQLException, DataBaseResponseException {
        this.title = title;
        this.description = description;
        this.expdbid = getExpDbId(secretid);
    }

    private int getExpDbId(String secretid) throws SQLException, DataBaseResponseException {
        int dbid = DBService.INSTANCE.getExperimentId(secretid, title);
        if (dbid == -1) {
            dbid = DBService.INSTANCE.addExperiment(secretid, title, description);
        }
        return dbid;
    }
    
    public Run getRun(String title) {
        return runs.get(title);
    }

    private static class ExperimentXMLHandler extends DefaultHandler {
        Experiment experiment;
       
        private String secretid;
        private String title;
        private String description;
        private String species;
        private String breed;
        private String source;
        private String platform;
        
        private StringBuilder builder;
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case "runs":
                case "publications":
                    try {
                        experiment = new Experiment(secretid, title, description);
                    } catch (SQLException | DataBaseResponseException ex) {
                        throw new SAXException(ex);
                    }
                    secretid = null;
                    title = null;
                    description = null;
                    break;
                case "secretid":
                case "title":
                case "description":
                case "publication":
                case "species":
                case "breed":
                case "source":
                case "platform":
                    builder = new StringBuilder();
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case "secretid":
                    secretid = builder.toString();
                    builder = null;
                    break;
                case "title":
                    title = builder.toString();
                    builder = null;
                    break;
                case "description":
                    description = builder.toString();
                    builder = null;
                    break;
                case "species":
                    species = builder.toString();
                    builder = null;
                    break;
                case "breed":
                    breed = builder.toString();
                    builder = null;
                    break;
                case "source":
                    source = builder.toString();
                    builder = null;
                    break;
                case "platform":
                    platform = builder.toString();
                    builder = null;
                    break;
                case "run":
                    Run newrun;
                    try {
                        newrun = new Run(
                        experiment.expdbid, secretid, title, description,
                        Integer.parseInt(species) , breed, source, platform);
                    } catch (SQLException | DataBaseResponseException ex) {
                        throw new SAXException(ex);
                    }
                    experiment.runs.put(title, newrun);
                    secretid = null;
                    title = null;
                    description = null;
                    species = null;
                    breed = null;
                    source = null;
                    platform = null;
                    break;
                case "publication":
                    experiment.publications.add(builder.toString());
                    builder = null;
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (builder != null) {
                builder.append(ch, start, length);
            }
        }
        
    }
}
