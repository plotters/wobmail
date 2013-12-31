package net.xytra.wobmail.mailconn.manager;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import net.xytra.wobmail.application.Session;
import net.xytra.wobmail.mailconn.session.WobmailSession;
import net.xytra.wobmail.mailconn.session.pop3.Pop3WobmailSession;

public class Pop3WobmailSessionManager implements WobmailSessionManager
{
	private static Pop3WobmailSessionManager _instance = new Pop3WobmailSessionManager();

	private Map<String, WobmailSession> entries;

	private Pop3WobmailSessionManager() {
		this.entries = new HashMap<String, WobmailSession>();
	}

	public static Pop3WobmailSessionManager instance() {
		return (_instance);
	}

	// Registration
	@Override
	public void deregisterEntry(String sessionId)
	{
		if (sessionId == null)
			return;

		WobmailSession entry = (WobmailSession)entries.get(sessionId);
		if (entry == null)
			return;

		entry.closeSession();
		entries.remove(sessionId);
	}

	@Override
	public void registerMailSession(Session session, String username, String password) throws MessagingException 
	{
		Pop3WobmailSession entry = new Pop3WobmailSession(username, password);

		// Try a login before we do anything else
		entry.keepConnectionOpen(true);

		// Logging in to server worked, remember mail session
		entries.put(session.sessionID(), entry);

		// Setup the mail session in the WO Session for easy access
		session.setMailSession(entry);
	}

}
