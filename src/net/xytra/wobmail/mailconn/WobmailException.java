package net.xytra.wobmail.mailconn;

public class WobmailException extends RuntimeException {
	public WobmailException() {
		super();
	}

	public WobmailException(String message, Throwable cause) {
		super(message, cause);
	}

	public WobmailException(String message) {
		super(message);
	}

	public WobmailException(Throwable cause) {
		super(cause);
	}

}
