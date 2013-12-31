package net.xytra.wobmail.mailconn.folder.pop3;

import java.util.Enumeration;

import javax.mail.Message;
import javax.mail.MessagingException;

import net.xytra.wobmail.mailconn.WobmailException;
import net.xytra.wobmail.mailconn.folder.WobmailFolder;
import net.xytra.wobmail.mailconn.folder.WobmailFolderType;
import net.xytra.wobmail.mailconn.message.WobmailMessage;
import net.xytra.wobmail.mailconn.message.pop3.Pop3WobmailMessage;
import net.xytra.wobmail.mailconn.session.MailSession;

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
	private NSDictionary<Integer, WobmailMessage> cachedMessageRows = null;
	private NSArray<Integer> cachedSortedMessageNumbers = null;
	private NSArray<WobmailMessage> cachedSortedMessageRows = null;

	public Pop3WobmailFolder(MailSession mailSession, String folderName) {
		this.mailSession = mailSession;
		this.folderName = folderName;
	}

	@Override
	public WobmailMessage getMessageByIndex(int index) {
		return (getMessages().objectAtIndex(index));
	}

	@Override
	public NSArray<WobmailMessage> getMessages() {
		return (getMessages(false));
	}

	@Override
	public NSArray<WobmailMessage> getMessages(boolean reloadMessageList) {
		// forceReload == true means that we have to invalidate the message numbers and rows
		if (reloadMessageList) {
			cachedSortedMessageNumbers = null;
			cachedSortedMessageRows = null;
		}

		if ((cachedSortedMessageNumbers == null) || (cachedSortedMessageRows == null)) {
			NSDictionary<Integer, WobmailMessage> messageRowsDictionary = getMessageRowDictionary(reloadMessageList);

			if (cachedSortedMessageNumbers == null) {
				cachedSortedMessageNumbers = getMessageNumbersSorted(messageRowsDictionary);
	
				// Invalidate cached inbox rows
				cachedSortedMessageRows = null;
			}

			if (cachedSortedMessageRows == null) {
				cachedSortedMessageRows = getOrderedMessageRows(messageRowsDictionary, cachedSortedMessageNumbers);
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

	public void moveMessagesToFolder(NSArray<WobmailMessage> messageRows, String folderName) {
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
	public void sortMessagesWithKey(String sortKey, boolean reverseSort) {
		String currentSortKey = getSortKey();
		boolean currentReverseSort = isReverseSort();

		// Set the new parameters:
		setSortKey(sortKey);
		setReverseSort(reverseSort);

		if (!currentSortKey.equals(sortKey)) {
			// Sort key has changed, just invalidate cache
			cachedSortedMessageNumbers = null;
			cachedSortedMessageRows = null;
		} else if (reverseSort != currentReverseSort) {
			// Sort key hasn't changed; only reverse the order:
			cachedSortedMessageNumbers = ERXArrayUtilities.reverse(cachedSortedMessageNumbers);
			cachedSortedMessageRows = ERXArrayUtilities.reverse(cachedSortedMessageRows);
		}
	}

	//=========================================================================
	// Supporting utility methods
	protected NSArray<WobmailMessage> getFreshUnsortedMessages() {
		NSArray<WobmailMessage> unsortedMessageRows;

		// Only allow one such access at a time through this session
		synchronized (this) {
			// Get all messages from INBOX
			Message[] messages;
			try {
				messages = mailSession.obtainOpenFolder(folderName).getMessages();
			} catch (MessagingException e) {
				throw (new WobmailException(e));
			}

			NSMutableArray<WobmailMessage> messageRowsArray = new NSMutableArray<WobmailMessage>();

			// Let's get each message in a wrapper and keep it all for future use:
			for (int i=0; i<messages.length; i++) {
				messageRowsArray.addObject(new Pop3WobmailMessage(messages[i]));
			}
			
			unsortedMessageRows = messageRowsArray.immutableClone();
		}

		return (unsortedMessageRows);
	}

	protected NSArray<Integer> getMessageNumbersSorted(NSDictionary<Integer, WobmailMessage> messages) {
		return ((NSArray<Integer>)ERXArrayUtilities.sortedArraySortedWithKey(
				messages.allValues(),
				getSortKey(),
				isReverseSort() ?
						EOSortOrdering.CompareCaseInsensitiveDescending :
						EOSortOrdering.CompareCaseInsensitiveAscending).valueForKey("messageNumber"));
	}

	protected NSDictionary<Integer, WobmailMessage> getMessageRowDictionary(boolean forceReload) {
		if (forceReload || (cachedMessageRows == null)) {
			NSMutableDictionary<Integer, WobmailMessage> newMessageRowDict = new NSMutableDictionary<Integer, WobmailMessage>();

			Enumeration<WobmailMessage> en1 = getFreshUnsortedMessages().objectEnumerator();
			while (en1.hasMoreElements()) {
				WobmailMessage message = en1.nextElement();
				newMessageRowDict.setObjectForKey(message, message.getMessageNumber());
			}

			// Save our new dictionary and set the return value
			cachedMessageRows = newMessageRowDict;
		}

		return (cachedMessageRows);
	}

	/**
	 * @param messageRows an <code>NSDictionary</code> of <code>MessageRow</code>s, each for its index corresponding to its natural order in the folder.
	 * @param messageNumbers the <code>NSArray</code> of the desired order. 
	 * @return an <code>NSArray</code> of <code>MessageRow</code>s provided in messageRows but in the order specified in messageNumbers.
	 */
	protected NSArray<WobmailMessage> getOrderedMessageRows(NSDictionary<Integer, WobmailMessage> messageRows, NSArray<Integer> messageNumbers) {
		NSMutableArray<WobmailMessage> orderedMessageRows = new NSMutableArray<WobmailMessage>();

		Enumeration<Integer> en1 = messageNumbers.objectEnumerator();
		while (en1.hasMoreElements()) {
			orderedMessageRows.addObject(messageRows.objectForKey(en1.nextElement()));
		}

		return (orderedMessageRows);
	}

	// delete/trash
	protected void deleteMessage(WobmailMessage message) throws MessagingException {
		message.setIsDeleted(true);

		cachedMessageRows.remove(message.getMessageNumber());
		cachedSortedMessageNumbers.remove(message.getMessageNumber());
		cachedSortedMessageRows.remove(message);
	}

	synchronized protected void deleteMessages(NSArray<WobmailMessage> messageRows) throws MessagingException {
		Enumeration<WobmailMessage> en1 = messageRows.objectEnumerator();
		
		while (en1.hasMoreElements()) {
			deleteMessage(en1.nextElement());
		}
	}

	protected void moveMessageRowsToTrash(NSArray<WobmailMessage> messageRows) throws MessagingException {
		deleteMessages(messageRows);
	}

	// Sorting and reverse
	private String sortKey = WobmailMessage.DATE_SENT_SORT_FIELD;
	private boolean isReverseSort = false;

	@Override
	public String getSortKey() {
		return (sortKey);
	}

	protected void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	@Override
	public boolean isReverseSort() {
		return (isReverseSort);
	}

	protected void setReverseSort(boolean reverse) {
		isReverseSort = Boolean.valueOf(reverse);
	}

}
