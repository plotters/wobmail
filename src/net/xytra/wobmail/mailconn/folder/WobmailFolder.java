package net.xytra.wobmail.mailconn.folder;

import net.xytra.wobmail.misc.MessageRow;

import com.webobjects.foundation.NSArray;

/**
 * @author jonny.meijer
 *
 */
public interface WobmailFolder {

	public MessageRow getMessageRowByIndex(int index);
	/**
	 * @return an <code>NSArray</code> of <code>WobmailMessage</code>s representing the messages in this folder.
	 */
	public NSArray<MessageRow> getMessages();

	/**
	 * @param reloadMessageList whether to force a reload of the message list by querying the mail server
	 * @return an <code>NSArray</code> of <code>WobmailMessage</code>s representing the messages in this folder.
	 */
	public NSArray<MessageRow> getMessages(boolean reloadMessageList);

	/**
	 * @return this folder's name.
	 */
	public String getName();

	/**
	 * @return number of messages in this folder.
	 */
	public int getNumberMessages();

	/**
	 * @return this folder's current sort key (column name by which to sort).
	 */
	public String getSortKey();

	/**
	 * @return whether this folder sorting is currently reversed.
	 */
	public boolean isReverseSort();

	/**
	 * Move messages to the specified folder.
	 * TODO: Should use WobmailFolder instead of String to specify folder.
	 * @param messageRows
	 * @param folderName
	 */
	public void moveMessageRowsToFolder(NSArray<MessageRow> messageRows, String folderName);

	/**
	 * Sort this folder's message list and return the newly sorted list.
	 * Sort this folder's message list using specified sorting key; do a
	 * reverse sort if reverseSort is true.
	 *
	 * @param sortKey Key representing which message property by which to sort.
	 * @param reverseSort Whether to reverse sort.
	 */
	public void sortMessageRowsWithKey(String sortKey, boolean reverseSort);

}
