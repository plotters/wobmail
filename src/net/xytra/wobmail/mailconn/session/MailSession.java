package net.xytra.wobmail.mailconn.session;

import javax.mail.Folder;
import javax.mail.internet.MimeMessage;

import net.xytra.wobmail.mailconn.folder.WobmailFolder;
import net.xytra.wobmail.misc.MessageRow;

public interface MailSession {

	public void closeSession();

	/**
	 * TODO: This method is a temporary convenience method.
	 * @return
	 */
	@Deprecated
	public WobmailFolder getInboxFolder();

	/**
	 * Ensures the connection to the mail Store is kept open or reopened for the subsequent query of the message specified. 
	 * @param messageRow
	 */
	public void keepConnectionOpenForMessage(MessageRow messageRow);

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
