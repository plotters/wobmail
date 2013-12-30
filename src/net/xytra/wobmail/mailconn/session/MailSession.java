package net.xytra.wobmail.mailconn.session;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import net.xytra.wobmail.misc.MessageRow;

import com.webobjects.foundation.NSArray;

public interface MailSession {
	public static final String INBOX_FOLDER_NAME = "Inbox";
	public static final String TRASH_FOLDER_NAME = "Trash";

	public void closeSession();

	public MessageRow getMessageRowForFolderByIndex(String folderName, int index) throws MessagingException;

	public NSArray<MessageRow> getMessageRowsForFolder(String folderName) throws MessagingException;

	public NSArray<MessageRow> getMessageRowsForFolder(String folderName, boolean forceReload) throws MessagingException;

	public int getNumberMessagesInFolder(String folderName) throws MessagingException;

	public void keepConnectionOpenForMessage(Message message) throws MessagingException;

	public void moveMessageRowToFolder(MessageRow messageRow, String folderName) throws MessagingException;

	public void moveMessageRowsToFolder(NSArray<MessageRow> messageRows, String folderName) throws MessagingException;

	public MimeMessage obtainNewMimeMessage();

	/**
	 * Sort the specified folder's message list and return the newly sorted list.
	 * Sort the specified folder's message list using specified sorting key; do
	 * a reverse sort if reverseSort is true.
	 *
	 * @param folderName Name of the folder whose messages will be sorted.
	 * @param sortKey Key representing which message property by which to sort.
	 * @param reverseSort Whether to reverse sort.
	 * @return an NSArray of MessageRow objects, newly sorted.
	 */
	public void sortMessageRowsForFolderSortedWithKey(String folderName, String sortKey, boolean reverseSort) throws MessagingException;

	/**
	 * @param folderName name of the mail Folder.
	 * @return the sorting key currently used on the specified folder.
	 */
	public String sortKeyForFolder(String folderName);

	/**
	 * @param folderName name of the mail Folder
	 * @return whether reverse sorting is used on the current column in the specified folder.
	 */
	public boolean isReverseSortForFolder(String folderName);
}
