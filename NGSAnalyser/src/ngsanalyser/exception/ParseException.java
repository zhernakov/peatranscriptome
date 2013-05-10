package ngsanalyser.exception;

public class ParseException extends Exception {

    public ParseException() {
    }

    public ParseException(String msg) {
        super(msg);
    }

    public ParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
