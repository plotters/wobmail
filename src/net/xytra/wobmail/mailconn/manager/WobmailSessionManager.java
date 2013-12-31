package net.xytra.wobmail.mailconn.manager;

import javax.mail.MessagingException;

import net.xytra.wobmail.application.Session;

public interface WobmailSessionManager {
	/**
	 * Deregister the <code>WobmailSession</code> associated to the WebObjects <code>Session</code> specified. 
	 * @param sessionId id of the WebObjects <code>Session</code>.
	 */
	public void deregisterEntry(String sessionId);

	/**
	 * @param session
	 * @param username
	 * @param password
	 * @throws MessagingException if something went wrong when registering the mail session (authentication, etc.).
	 */
	public void registerMailSession(Session session, String username, String password) throws MessagingException;

}
