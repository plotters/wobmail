package net.xytra.wobmail.mailconn.folder.pop3;

import java.util.Enumeration;

import javax.mail.Message;
import javax.mail.MessagingException;

import net.xytra.wobmail.mailconn.WobmailException;
import net.xytra.wobmail.mailconn.folder.WobmailFolder;
import net.xytra.wobmail.mailconn.folder.WobmailFolderType;
import net.xytra.wobmail.mailconn.session.MailSession;
import net.xytra.wobmail.misc.MessageRow;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXArrayUtilities;

/**
 * @author jonny.meijer
 *
 */
public class Pop3WobmailFolder implements WobmailFolder {

	// Associated session and absolute folder name
	private final MailSession mailSession;
	private final String folderName;

	// Folder contents cached
	private NSDictionary<Integer, MessageRow> cachedInboxMessageRows = null;
	private NSArray<Integer> cachedSortedxMessageNumbers = null;
	private NSArray<MessageRow> cachedSortedMessageRows = null;

	public Pop3WobmailFolder(MailSession mailSession, String folderName) {
		this.mailSession = mailSession;
		this.folderName = folderName;
	}

	@Override
	public MessageRow getMessageRowByIndex(int index) {
		return (getMessages().objectAtIndex(index));
	}

	@Override
	public NSArray<MessageRow> getMessages() {
		return (getMessages(false));
	}

	@Override
	public NSArray<MessageRow> getMessages(boolean reloadMessageList) {
		// forceReload == true means that we have to invalidate the message numbers and rows
		if (reloadMessageList) {
			cachedSortedxMessageNumbers = null;
			cachedSortedMessageRows = null;
		}

		if ((cachedSortedxMessageNumbers == null) || (cachedSortedMessageRows == null)) {
			NSDictionary<Integer, MessageRow> messageRowsDictionary = getMessageRowDictionary(reloadMessageList);

			if (cachedSortedxMessageNumbers == null) {
				cachedSortedxMessageNumbers = getMessageNumbersSorted(messageRowsDictionary);
	
				// Invalidate cached inbox rows
				cachedSortedMessageRows = null;
			}

			if (cachedSortedMessageRows == null) {
				cachedSortedMessageRows = getOrderedMessageRows(messageRowsDictionary, cachedSortedxMessageNumbers);
			}
		}

		return (cachedSortedMessageRows);
	}

	@Override
	public String getName() {
		// TODO: Implement properly
		return "Inbox";
	}

	@Override
	public int getNumberMessages() {
		return (getMessages().count());
	}

	public void moveMessageRowsToFolder(NSArray<MessageRow> messageRows, String folderName) {
		if (!WobmailFolderType.TRASH.name().equals(folderName)) {
			throw (new WobmailException("Can only move to Trash in POP3.  Not allowed to use folderName="+folderName));
		}

		try {
			moveMessageRowsToTrash(messageRows);
		} catch (MessagingException e) {
			throw (new WobmailException(e));
		}
	}

	/**
	 * Sort this folder's message list and return the newly sorted list.
	 * Sort this folder's message list using specified sorting key; do a
	 * reverse sort if reverseSort is true.
	 *
	 * @param sortKey Key representing which message property by which to sort.
	 * @param reverseSort Whether to reverse sort.
	 */
	public void sortMessageRowsWithKey(String sortKey, boolean reverseSort) {
		String currentSortKey = getSortKey();
		boolean currentReverseSort = isReverseSort();

		// Set the new parameters:
		setSortKey(sortKey);
		setReverseSort(reverseSort);

		if (!currentSortKey.equals(sortKey)) {
			// Sort key has changed, just invalidate cache
			cachedSortedxMessageNumbers = null;
			cachedSortedMessageRows = null;
		} else if (reverseSort != currentReverseSort) {
			// Sort key hasn't changed; only reverse the order:
			cachedSortedxMessageNumbers = ERXArrayUtilities.reverse(cachedSortedxMessageNumbers);
			cachedSortedMessageRows = ERXArrayUtilities.reverse(cachedSortedMessageRows);
		}
	}

	//=========================================================================
	// Supporting utility methods
	protected NSArray<MessageRow> getFreshUnsortedMessageRows() {
		NSArray<MessageRow> unsortedMessageRows;

		// Only allow one such access at a time through this session
		synchronized (this) {
			// Get all messages from INBOX
			Message[] messages;
			try {
				messages = mailSession.obtainOpenFolder(folderName).getMessages();
			} catch (MessagingException e) {
				throw (new WobmailException(e));
			}

			NSMutableArray<MessageRow> messageRowsArray = new NSMutableArray<MessageRow>();

			// Let's get each message in a wrapper and keep it all for future use:
			for (int i=0; i<messages.length; i++) {
				messageRowsArray.addObject(new MessageRow(messages[i]));
			}
			
			unsortedMessageRows = messageRowsArray.immutableClone();
		}

		return (unsortedMessageRows);
	}

	protected NSArray<Integer> getMessageNumbersSorted(NSDictionary<Integer, MessageRow> messageRows) {
		return ((NSArray<Integer>)ERXArrayUtilities.sortedArraySortedWithKey(
				messageRows.allValues(),
				getSortKey(),
				isReverseSort() ?
						EOSortOrdering.CompareCaseInsensitiveDescending :
						EOSortOrdering.CompareCaseInsensitiveAscending).valueForKey("messageNumber"));
	}

	protected NSDictionary<Integer, MessageRow> getMessageRowDictionary(boolean forceReload) {
		if (forceReload || (cachedInboxMessageRows == null)) {
			NSMutableDictionary<Integer, MessageRow> newMessageRowDict = new NSMutableDictionary<Integer, MessageRow>();

			Enumeration<MessageRow> en1 = getFreshUnsortedMessageRows().objectEnumerator();
			while (en1.hasMoreElements()) {
				MessageRow mr = en1.nextElement();
				newMessageRowDict.setObjectForKey(mr, mr.getMessageNumber());
			}

			// Save our new dictionary and set the return value
			cachedInboxMessageRows = newMessageRowDict;
		}

		return (cachedInboxMessageRows);
	}

	/**
	 * @param messageRows an <code>NSDictionary</code> of <code>MessageRow</code>s, each for its index corresponding to its natural order in the folder.
	 * @param messageNumbers the <code>NSArray</code> of the desired order. 
	 * @return an <code>NSArray</code> of <code>MessageRow</code>s provided in messageRows but in the order specified in messageNumbers.
	 */
	protected NSArray<MessageRow> getOrderedMessageRows(NSDictionary<Integer, MessageRow> messageRows, NSArray<Integer> messageNumbers) {
		NSMutableArray<MessageRow> orderedMessageRows = new NSMutableArray<MessageRow>();

		Enumeration<Integer> en1 = messageNumbers.objectEnumerator();
		while (en1.hasMoreElements()) {
			orderedMessageRows.addObject(messageRows.objectForKey(en1.nextElement()));
		}

		return (orderedMessageRows);
	}

	// delete/trash
	protected void deleteMessageRow(MessageRow messageRow) throws MessagingException {
		messageRow.setIsDeleted(true);

		cachedInboxMessageRows.remove(messageRow.getMessageNumber());
		cachedSortedxMessageNumbers.remove(messageRow.getMessageNumber());
		cachedSortedMessageRows.remove(messageRow);
	}

	synchronized protected void deleteMessageRows(NSArray<MessageRow> messageRows) throws MessagingException {
		Enumeration<MessageRow> en1 = messageRows.objectEnumerator();
		
		while (en1.hasMoreElements()) {
			deleteMessageRow(en1.nextElement());
		}
	}

	protected void moveMessageRowsToTrash(NSArray<MessageRow> messageRows) throws MessagingException {
		deleteMessageRows(messageRows);
	}

	// Sorting and reverse
	private String sortKey = MessageRow.DATE_SENT_SORT_FIELD;
	private boolean isReverseSort = false;

	public String getSortKey() {
		return (sortKey);
	}

	protected void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	public boolean isReverseSort() {
		return (isReverseSort);
	}

	protected void setReverseSort(boolean reverse) {
		isReverseSort = Boolean.valueOf(reverse);
	}

}
