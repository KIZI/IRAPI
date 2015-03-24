package eu.linkedtv.irapi.search.errorhandling;

import javax.ws.rs.core.Response.Status;

public class IrapiException extends Exception {
	private static final long serialVersionUID = 1799205132647582576L;
	private final Status status;
	private final Throwable cause;

	public IrapiException(final String message, final Status status, final Exception cause) {
		super(message);
		this.status = status;
		this.cause = cause;
	}

	public Status getStatus() {
		return status;
	}

	@Override
	public Throwable getCause() {
		return cause;
	}

}
