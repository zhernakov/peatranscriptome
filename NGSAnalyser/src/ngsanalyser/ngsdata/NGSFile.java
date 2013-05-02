package ngsanalyser.ngsdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

abstract public class NGSFile implements NGSCollectable {
    public static NGSFile NGSFileFactory(String filepath, NGSFileType type) throws NGSFileException {
        switch (type) {
            case FASTQ_SANGER:
            case FASTQ_SRA:
            case FASTQ_SOLEXA:
            case FASTQ_ILLUMINA:
                return new FASTQFile(filepath, type);
            default:
                throw new NGSFileUndefinedTypeException("Can't deal with sequencing result file " + filepath);
        }
    }

    public static NGSFile NGSFileFactory(String filepath) throws NGSFileException {
        return NGSFileFactory(filepath, NGSFileType.defineType(filepath));
    }

    protected static FileInputStream getStream(String filepath) throws NGSFileNotFoundException {
        try {
            return new FileInputStream(new File(filepath));
        } catch (FileNotFoundException e) {
            throw new NGSFileNotFoundException("Can't open sequencing result file " + filepath);
        }
    }

    @Override
    abstract public NGSRecord getNGSRecord();
}
