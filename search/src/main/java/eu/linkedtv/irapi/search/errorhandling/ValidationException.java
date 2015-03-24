package eu.linkedtv.irapi.search.errorhandling;

public class ValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ValidationException(final String message) {
		super(message);
	}

}
