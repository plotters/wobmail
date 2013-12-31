package net.xytra.wobmail.mailconn.session;

import javax.mail.Folder;
import javax.mail.internet.MimeMessage;

import net.xytra.wobmail.mailconn.folder.WobmailFolder;
import net.xytra.wobmail.mailconn.message.WobmailMessage;

public interface WobmailSession {

	/**
	 * Close this mail session.
	 */
	public void closeSession();

	/**
	 * Get a new <code>WobmailFolder</code> for the current account's inbox.
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
