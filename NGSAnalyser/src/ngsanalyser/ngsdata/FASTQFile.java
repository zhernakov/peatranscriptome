package ngsanalyser.ngsdata;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import org.biojava3.core.sequence.io.util.IOUtils;
import org.biojava3.sequencing.io.fastq.*;

public class FASTQFile extends NGSFile {
    public static boolean isFASTQ(NGSFileType type, String filepath) 
            throws NGSFileNotFoundException {
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

    private static FastqReader getFASTQReader(NGSFileType type) 
            throws NGSFileException {
        switch (type) {
            case FASTQ_SANGER:
            case FASTQ_SRA:
                return new SangerFastqReader();
            case FASTQ_SOLEXA:
                return new SolexaFastqReader();
            case FASTQ_ILLUMINA:
                return new IlluminaFastqReader();
            default:
                throw new NGSFileWrongTypeException("There is the try to get "
                        + "FASTQReader for inappropriate type " + type);
        }
    }

    private static int countReads(FastqReader reader, String filepath) 
            throws NGSFileException {
        final FileInputStream is = getStream(filepath);
        int count = 0;
        try {
            for (final Fastq fastq : reader.read(is)) {
                ++count;
            }
            is.close();
        } catch (IOException e) {
            throw new NGSFileIOException(e.getMessage());
        } finally {
            IOUtils.close(is);
        }
        return count;
    }
    
    private final String filepath;
    private final int totalnumber;
    private final Iterator<Fastq> iterator;
    
    public FASTQFile(String filepath, NGSFileType type) throws NGSFileException {
        this.filepath = filepath;
        try {
            final FastqReader reader = getFASTQReader(type);
            totalnumber = countReads(reader, filepath);
            iterator = reader.read(getStream(filepath)).iterator();
        } catch (NGSFileWrongTypeException e) {
            throw new NGSFileWrongTypeException("Specified type " + type + 
                    " doesn't match type of sequencing data file " + filepath);
        } catch (IOException e) {
            throw new NGSFileIOException(e.getMessage());
        }
    }

    @Override
    public NGSRecord next() {
        if (iterator.hasNext()) {
            final Fastq fastq = iterator.next();
            return new NGSRecord(
                    fastq.getDescription(), 
                    fastq.getSequence(), 
                    fastq.getQuality()
            );
        } else {
            return null;
        }
    }

}
