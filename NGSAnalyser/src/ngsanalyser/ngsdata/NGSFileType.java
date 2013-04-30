package ngsanalyser.ngsdata;

import ngsanalyser.ngsdata.exception.NGSFileException;

public enum NGSFileType {
    FASTQ_SANGER,
    FASTQ_SOLEXA,
    FASTQ_ILLUMINA,
    FASTQ_SRA;
    
    public static NGSFileType defineType(String filepath) throws NGSFileException {
        if (FASTQFile.isFASTQ(FASTQ_SOLEXA, filepath)) {
            return FASTQ_SOLEXA;
        } else if (FASTQFile.isFASTQ(FASTQ_SANGER, filepath)) {
            return FASTQ_SANGER;
        } else if (FASTQFile.isFASTQ(FASTQ_ILLUMINA, filepath)) {
            return FASTQ_ILLUMINA;
        }
        return null;
    }

    
}
