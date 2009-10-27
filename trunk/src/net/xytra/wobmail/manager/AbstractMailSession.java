package net.xytra.wobmail.manager;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import net.xytra.wobmail.application.Application;
import net.xytra.wobmail.misc.MessageRow;

import com.webobjects.foundation.NSArray;

import er.extensions.ERXLogger;

public abstract class AbstractMailSession implements MailSession
{
	private TimerTask closeSessionTask;
	private Session mailSession;
	private Timer sessionTimer;
	private Store store;

	private String username;
	private String password;

	public AbstractMailSession(String username, String password)
	{
		this.username = username;
		this.password = password;

		this.sessionTimer = new Timer("AbstractMailSession Timer");
	}

	// Connection
	/**
	 * Obtain open connection to Store.
	 * @return whether the connection to Store is really open.
	 * @throws MessagingException
	 */
	boolean keepConnectionOpen() throws MessagingException {
		// Deschedule closeSessionTask
		cancelCloseSessionTask();

		boolean isConnectionToStoreOpen = getOpenStore().isConnected();

		// Reschedule closeSessionTask
		scheduleCloseSessionTask();

		return (isConnectionToStoreOpen);
	}

	// Session
	synchronized public void closeSession()
	{
		Enumeration<Folder> openFoldersEnumeration = getOpenFolders().objectEnumerator();

		while (openFoldersEnumeration.hasMoreElements()) {
			Folder folder = openFoldersEnumeration.nextElement();

			try {
				if (folder.isOpen())
					folder.close(true);
			}
			catch (MessagingException e) {
				e.printStackTrace();
			}
		}

		// Close store
		try {
			getStore().close();
		}
		catch (MessagingException e) {}

		// Forget all previously open folders
		forgetOpenFolders();
	}

	protected Session getSession()
	{
		if (mailSession == null) {
			mailSession = Session.getInstance(new Properties());
		}

		return (mailSession);
	}

	// Store
	protected Store getOpenStore() throws MessagingException
	{
		Store mailStore = getStore();

		if (!mailStore.isConnected()) {
			ERXLogger.log.debug("About to connect to store...");
			System.err.println("About to connect to store...");
			mailStore.connect(
					((Application)Application.application()).getDefaultIncomingMailServerAddress(),
					this.username,
					this.password);
		}

		return (mailStore);
	}

	/**
	 * @return a cached Store instance, with no effort to ensure it's open.
	 * @throws NoSuchProviderException
	 */
	protected Store getStore() throws NoSuchProviderException {
		if (store == null) {
			store = getSession().getStore(getMailProtocolName());
		}

		return (store);
	}

	protected abstract String getMailProtocolName();

	// Folders
	protected abstract void forgetOpenFolders();

	protected abstract NSArray getOpenFolders();

	// Messages
	public MessageRow getMessageRowForFolderWithName(int index, String folderName) throws MessagingException {
		return (getMessageRowsForFolderWithName(folderName).objectAtIndex(index));
	}

	public int getNumberMessagesInFolderWithName(String folderName) throws MessagingException {
		return (getMessageRowsForFolderWithName(folderName).size());
	}

	public void moveMessageRowToFolderWithName(MessageRow messageRow, String folderName) throws MessagingException {
		moveMessageRowsToFolderWithName(new NSArray<MessageRow>(messageRow), folderName);
	}

	public MimeMessage obtainNewMimeMessage() {
		return (new MimeMessage(mailSession));
	}

	/* closeSessionTask-related methods */
	public void cancelCloseSessionTask()
	{
		if (closeSessionTask == null) {
			return;
		}

		ERXLogger.log.debug("cancelCloseSessionTask() at " + System.currentTimeMillis());
		System.err.println("cancelCloseSessionTask() at " + System.currentTimeMillis());
		closeSessionTask.cancel();
		closeSessionTask = null;
	}

	public void scheduleCloseSessionTask()
	{
		if (closeSessionTask != null) {
			return;
		}

		ERXLogger.log.debug("scheduleCloseSessionTask() at " + System.currentTimeMillis());
		System.err.println("scheduleCloseSessionTask() at " + System.currentTimeMillis());
		closeSessionTask = new CloseStoreTimerTask(this); 
		sessionTimer.schedule(closeSessionTask, 30000l);			
	}

	private class CloseStoreTimerTask extends TimerTask
	{
		private MailSession session;

		public CloseStoreTimerTask(MailSession session) {
			this.session = session;
		}

		@Override
		public void run() {
			ERXLogger.log.debug("Closing the session!");
			System.err.println("Closing the session!");
			this.session.closeSession();
		}
	}

}
