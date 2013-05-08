package ngsanalyser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import ngsanalyser.exception.ParsingException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Experiment {
    public static Experiment createInstance(String path) throws FileNotFoundException, IOException, ParsingException {
        try {
            final SAXParserFactory parserfactory = SAXParserFactory.newInstance();
            final SAXParser parser = parserfactory.newSAXParser();
            final ExperimentXMLHandler handler = new ExperimentXMLHandler();
            final InputStream is = new FileInputStream(new File(path));
            parser.parse(is, handler);
            return handler.experiment;
        } catch (ParserConfigurationException | SAXException ex) {
            throw new ParsingException("Experiment file is corupted");
        }
    }
    
    private String title;
    private String description;
    private Map<String,Run> runs = new TreeMap<>();
    private List<String> publications = new LinkedList<>();

    private Experiment() {
        
    }
    
    private void setTitle(String title) {
        this.title = title;
    }

    private void setDescription(String description) {
        this.description = description;
    }
    
    private void addRun(Run run) throws SAXException {
        final String id = run.getTitle();
        if (runs.containsKey(id)) {
            throw new SAXException("");
        }
        runs.put(id, run);
    }
    
    private void addPublication(String publication) {
        publications.add(publication);
    }
    
    public static class Run {
        private String  title;
        private String  description;
        private int     species;
        private String  breed;
        private String  source;
        private String  platform;
        
        public Run() {
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setSpecies(int species) {
            this.species = species;
        }

        public void setBreed(String breed) {
            this.breed = breed;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getTitle() {
            return title;
        }
        
    }

    private static class ExperimentXMLHandler extends DefaultHandler {
        Experiment experiment;
        private Run run;
        private StringBuilder builder;
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case "experiment":
                    experiment = new Experiment();
                    break;
                case "run":
                    run = new Run();
                    break;
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
            if (run == null) {
                switch (qName) {
                    case "title":
                        experiment.setTitle(builder.toString());
                        break;
                    case "description":
                        experiment.setDescription(builder.toString());
                        break;
                    case "publication":
                        experiment.addPublication(builder.toString());
                        break;
                }
                builder = null;
            } else {
                switch (qName) {
                    case "run":
                        experiment.addRun(run);
                        run = null;
                        break;
                    case "title":
                        run.setTitle(builder.toString());
                        break;
                    case "description":
                        run.setDescription(builder.toString());
                        break;
                    case "species":
                        run.setSpecies(Integer.parseInt(builder.toString()));
                        break;
                    case "breed":
                        run.setBreed(builder.toString());
                        break;
                    case "source":
                        run.setSource(builder.toString());
                        break;
                    case "platform":
                        run.setPlatform(builder.toString());
                        break;
                }
                builder = null;
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
