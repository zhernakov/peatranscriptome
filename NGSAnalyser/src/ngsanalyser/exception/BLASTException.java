/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ngsanalyser.exception;

/**
 *
 * @author Саша
 */
public class BLASTException extends Exception {

    /**
     * Creates a new instance of
     * <code>BLASTException</code> without detail message.
     */
    public BLASTException() {
    }

    /**
     * Constructs an instance of
     * <code>BLASTException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public BLASTException(String msg) {
        super(msg);
    }
}
