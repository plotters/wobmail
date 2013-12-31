package net.xytra.wobmail.manager;

public class MailSessionException extends RuntimeException {
	public MailSessionException() {
		super();
	}

	public MailSessionException(String message, Throwable cause) {
		super(message, cause);
	}

	public MailSessionException(String message) {
		super(message);
	}

	public MailSessionException(Throwable cause) {
		super(cause);
	}

}