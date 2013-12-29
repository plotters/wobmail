package net.xytra.wobmail.manager;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import net.xytra.wobmail.application.Application;
import net.xytra.wobmail.misc.MessageRow;

import com.webobjects.foundation.NSArray;

import er.extensions.logging.ERXLogger;

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
	synchronized boolean keepConnectionOpen(boolean reschedule) throws MessagingException {
		System.err.println("keepConnectionOpen("+reschedule+") ...");

		// Deschedule closeSessionTask
		if (reschedule) {
			cancelCloseSessionTask();
		}

		boolean isConnectionToStoreOpen = getOpenStore().isConnected();

		// Reschedule closeSessionTask
		if (reschedule) {
			scheduleCloseSessionTask();
		}

		return (isConnectionToStoreOpen);
	}

	public void keepConnectionOpenForMessage(Message message) throws MessagingException {
		if (keepConnectionOpen(false)) {
			obtainOpenFolder(message.getFolder());
		} else {
			throw (new MailSessionException("Could not get folder open: " + message.getFolder()));
		}
	}

	/**
	 * @param folder
	 * @return the same folder as passed in, open as READ_WRITE.
	 * @throws MessagingException
	 */
	synchronized protected Folder obtainOpenFolder(Folder folder) throws MessagingException {
		if (!folder.isOpen()) {
			folder.open(Folder.READ_WRITE);
		}

		return (folder);
	}

	// Session
	synchronized public void closeSession()
	{
		System.err.println("closeSession()...");

		// Start by cancelling any timed event:
		cancelCloseSessionTask();

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
			mailStore.connect(
					((Application)Application.application()).getDefaultIncomingMailServerAddress(),
					this.username,
					this.password);

			scheduleCloseSessionTask();
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

	protected abstract NSArray<Folder> getOpenFolders();

	// Messages
	public MessageRow getMessageRowForFolderByIndex(String folderName, int index) throws MessagingException {
		return (getMessageRowsForFolder(folderName).objectAtIndex(index));
	}

	public NSArray<MessageRow> getMessageRowsForFolder(String folderName) throws MessagingException {
		return (getMessageRowsForFolder(folderName, false));
	}

	public int getNumberMessagesInFolder(String folderName) throws MessagingException {
		return (getMessageRowsForFolder(folderName).size());
	}

	public void moveMessageRowToFolder(MessageRow messageRow, String folderName) throws MessagingException {
		moveMessageRowsToFolder(new NSArray<MessageRow>(messageRow), folderName);
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
		closeSessionTask.cancel();
		closeSessionTask = null;
	}

	public void scheduleCloseSessionTask()
	{
		if (closeSessionTask != null) {
			return;
		}

		ERXLogger.log.debug("scheduleCloseSessionTask() at " + System.currentTimeMillis());
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
			this.session.closeSession();
		}
	}

}
