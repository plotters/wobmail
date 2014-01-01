package net.xytra.wobmail.mailconn.session;

import javax.mail.Folder;
import javax.mail.internet.MimeMessage;

import com.webobjects.foundation.NSArray;

import net.xytra.wobmail.mailconn.folder.WobmailFolder;
import net.xytra.wobmail.mailconn.message.WobmailMessage;

public interface WobmailSession {

	/**
	 * Close this mail session.
	 */
	public void closeSession();

	/**
	 * Tell this WobmailSession to deregister itself for the WebObject <code>Session</code> specified.
	 * @param sessionID WebObjects <code>Session<code> ID.
	 */
	public void deregisterForWOSessionID(String sessionID);

	/**
	 * Get all <code>WobmailFolder</code>s under the root of this account.
	 * Usually includes inbox.
	 *
	 * @return all top-level folders.
	 */
	public NSArray<WobmailFolder> getFolders();

	/**
	 * Get a (possibly) new <code>WobmailFolder</code> for the current account's inbox.
	 * If multiple <code>WobmailFolder</code>s are created for the inbox, they
	 * will each have their own sorting/reverse setting and selection of
	 * messages.  This is probably not what you want, so avoid using this where
	 * possible.
	 * 
	 * @return a new WobmailFolder for the current account's Inbox.
	 */
	public WobmailFolder getInboxFolder();

	/**
	 * Ensures the connection to the mail Store is kept open or reopened for the subsequent query of the message specified. 
	 * @param message
	 */
	public void keepConnectionOpenForMessage(WobmailMessage message);

	/**
	 * @return a new, empty, Mime message.
	 */
	public MimeMessage obtainNewMimeMessage();

	// Folders
	/**
	 * This method is marked as deprecated because this is not the right interface for this method.
	 * @param folderName
	 * @return
	 */
	@Deprecated
	public Folder obtainOpenFolder(String folderName);

}
