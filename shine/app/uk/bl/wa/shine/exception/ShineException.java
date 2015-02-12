package uk.bl.wa.shine.exception;

public class ShineException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 866007261538659310L;

	public ShineException(String message) {
        super(message);
    }
	
	public ShineException(Throwable exception) {
		super(exception);
	}
}
