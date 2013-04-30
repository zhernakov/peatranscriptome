package ngsanalyser.ngsdata;

public abstract class NGSFileException extends Exception {

    public NGSFileException() {
    }

    public NGSFileException(String msg) {
        super(msg);
    }
}

class NGSFileNotFoundException extends NGSFileException {

    public NGSFileNotFoundException() {
    }

    public NGSFileNotFoundException(String msg) {
        super(msg);
    }
}

class NGSFileIOException extends NGSFileException {

    public NGSFileIOException() {
    }

    public NGSFileIOException(String msg) {
        super(msg);
    }
}

class NGSFileUndefinedTypeException extends NGSFileException {

    public NGSFileUndefinedTypeException() {
    }

    public NGSFileUndefinedTypeException(String msg) {
        super(msg);
    }
}

class NGSFileWrongTypeException extends NGSFileException {

    public NGSFileWrongTypeException() {
    }

    public NGSFileWrongTypeException(String msg) {
        super(msg);
    }
}
