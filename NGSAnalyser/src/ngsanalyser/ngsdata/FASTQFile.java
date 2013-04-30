package ngsanalyser.ngsdata;

import ngsanalyser.ngsdata.exception.NGSFileException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import static ngsanalyser.ngsdata.NGSFile.getStream;
import static ngsanalyser.ngsdata.NGSFileType.FASTQ_ILLUMINA;
import static ngsanalyser.ngsdata.NGSFileType.FASTQ_SANGER;
import static ngsanalyser.ngsdata.NGSFileType.FASTQ_SOLEXA;
import static ngsanalyser.ngsdata.NGSFileType.FASTQ_SRA;
import ngsanalyser.ngsdata.exception.NGSFileIOException;
import ngsanalyser.ngsdata.exception.NGSFileNotFoundException;
import ngsanalyser.ngsdata.exception.NGSFileWrongTypeException;
import org.biojava3.core.sequence.io.util.IOUtils;
import org.biojava3.sequencing.io.fastq.Fastq;
import org.biojava3.sequencing.io.fastq.FastqReader;
import org.biojava3.sequencing.io.fastq.IlluminaFastqReader;
import org.biojava3.sequencing.io.fastq.SangerFastqReader;
import org.biojava3.sequencing.io.fastq.SolexaFastqReader;

public class FASTQFile extends NGSFile {
    public static boolean isFASTQ(NGSFileType type, String filepath) throws NGSFileNotFoundException {
        FileInputStream is = null;
        try {
            is = getStream(filepath);
            getFASTQReader(type).read(is);
        } catch (NGSFileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return false;
        } finally {
            IOUtils.close(is);
        }
        return true;
    }

    private static FastqReader getFASTQReader(NGSFileType type) throws NGSFileException {
                switch (type) {
            case FASTQ_SANGER:
            case FASTQ_SRA:
                return new SangerFastqReader();
            case FASTQ_SOLEXA:
                return new SolexaFastqReader();
            case FASTQ_ILLUMINA:
                return new IlluminaFastqReader();
            default:
                throw new NGSFileWrongTypeException("There is the try to get FASTQReader for inappropriate type " + type);
        }
    }

    private final String filepath;
    private final Iterator<Fastq> it;
    
    FASTQFile(String filepath, NGSFileType type) throws NGSFileException {
        this.filepath = filepath;
        try {
            it = getFASTQReader(type).read(getStream(filepath)).iterator();
        } catch (IOException e) {
            throw new NGSFileIOException(e.getMessage());
        }
    }
    
}
