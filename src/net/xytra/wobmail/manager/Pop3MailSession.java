package net.xytra.wobmail.manager;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import net.xytra.wobmail.misc.MessageRow;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXArrayUtilities;


public class Pop3MailSession extends AbstractMailSession
{
	private Folder inboxFolder = null;

	public Pop3MailSession(String username, String password) {
		super(username, password);
	}

	// Folders
	synchronized protected Folder obtainOpenInboxFolder() throws MessagingException
	{
		if (inboxFolder == null) {
			inboxFolder = getOpenStore().getFolder("INBOX");
		}

		inboxFolder = obtainOpenFolder(inboxFolder);

		return (inboxFolder);
	}

	@Override
	protected String getMailProtocolName() {
		return ("pop3");
	}

	@Override
	protected void forgetOpenFolders() {
		inboxFolder = null;
	}

	@Override
	protected NSArray<Folder> getOpenFolders() {
		if (inboxFolder == null) {
			return (NSArray.EmptyArray);
		} else {
			return (new NSArray<Folder>(inboxFolder));
		}
	}

	// Folder contents
	private NSDictionary<Integer, MessageRow> cachedInboxMessageRows = null;
	private NSArray<Integer> cachedSortedInboxMessageNumbers = null;
	private NSArray<MessageRow> cachedSortedInboxMessageRows = null;

	/* (non-Javadoc)
	 * @see net.xytra.wobmail.manager.MailSession#getMessageRowsForFolder(java.lang.String, boolean)
	 * Return an array of MessageRow objects by using the cached ordered message numbers.
	 */
	public NSArray<MessageRow> getMessageRowsForFolder(String folderName, boolean forceReload) throws MessagingException {
		if (!INBOX_FOLDER_NAME.equals(folderName)) {
			throw (new MailSessionException("Cannot get MessageRow objects for specified folderName as such a folder does not exist"));
		}

		// forceReload == true means that we have to invalidate the message numbers and rows
		if (forceReload) {
			cachedSortedInboxMessageNumbers = null;
			cachedSortedInboxMessageRows = null;
		}

		if ((cachedSortedInboxMessageNumbers == null) || (cachedSortedInboxMessageRows == null)) {
			NSDictionary<Integer, MessageRow> messageRowsDictionary = getMessageRowDictionaryForFolder(folderName, forceReload);

			if (cachedSortedInboxMessageNumbers == null) {
				cachedSortedInboxMessageNumbers = getMessageNumbersSortedForFolder(messageRowsDictionary, folderName);
	
				// Invalidate cached inbox rows
				cachedSortedInboxMessageRows = null;
			}

			if (cachedSortedInboxMessageRows == null) {
				cachedSortedInboxMessageRows = getOrderedMessageRows(messageRowsDictionary, cachedSortedInboxMessageNumbers);
			}
		}

		return (cachedSortedInboxMessageRows);
	}

	protected NSArray<MessageRow> getOrderedMessageRows(NSDictionary<Integer, MessageRow> messageRows, NSArray<Integer> messageNumbers) {
		NSMutableArray<MessageRow> orderedMessageRows = new NSMutableArray<MessageRow>();

		Enumeration<Integer> en1 = messageNumbers.objectEnumerator();
		while (en1.hasMoreElements()) {
			orderedMessageRows.addObject(messageRows.objectForKey(en1.nextElement()));
		}

		return (orderedMessageRows);
	}

	protected NSArray<Integer> getMessageNumbersSortedForFolder(NSDictionary<Integer, MessageRow> messageRows, String folderName) {
		return ((NSArray<Integer>)ERXArrayUtilities.sortedArraySortedWithKey(
				messageRows.allValues(),
				sortKeyForFolder(folderName),
				isReverseSortForFolder(folderName) ?
						EOSortOrdering.CompareCaseInsensitiveDescending :
						EOSortOrdering.CompareCaseInsensitiveAscending).valueForKey("messageNumber"));
	}

	protected NSDictionary<Integer, MessageRow> getMessageRowDictionaryForFolder(String folderName, boolean forceReload) throws MessagingException {
		NSDictionary<Integer, MessageRow> dict = getCachedMessageRowDictionaryForFolder(folderName);

		if (forceReload || (dict == null)) {
			NSMutableDictionary<Integer, MessageRow> newMessageRowDict = new NSMutableDictionary<Integer, MessageRow>();

			Enumeration<MessageRow> en1 = getFreshUnsortedMessageRowsForInbox().objectEnumerator();
			while (en1.hasMoreElements()) {
				MessageRow mr = en1.nextElement();
				newMessageRowDict.setObjectForKey(mr, mr.getMessageNumber());
			}

			// Set the return value
			dict = newMessageRowDict;

			// Save our new dictionary
			setCachedMessageRowDictionaryForFolder(dict, folderName);
		}

		return (dict);
	}

	protected NSArray<MessageRow> getFreshUnsortedMessageRowsForInbox() throws MessagingException {
		NSArray<MessageRow> unsortedMessageRows;

		// Only allow one such access at a time through this session
		synchronized (this) {
			// Get all messages from INBOX 
			Message[] messages = obtainOpenInboxFolder().getMessages();
			NSMutableArray<MessageRow> messageRowsArray = new NSMutableArray<MessageRow>();

			// Let's get each message in a wrapper and keep it all for future use:
			for (int i=0; i<messages.length; i++) {
				messageRowsArray.addObject(new MessageRow(messages[i]));
			}
			
			unsortedMessageRows = messageRowsArray.immutableClone();
		}

		return (unsortedMessageRows);
	}

	protected NSDictionary<Integer, MessageRow> getCachedMessageRowDictionaryForFolder(String folderName) {
		if (MailSession.INBOX_FOLDER_NAME.equals(folderName)) {
			return (cachedInboxMessageRows);
		} else {
			throw (new MailSessionException("Cannot set MessageRow objects for specified folderName as such a folder does not exist"));
		}
	}

	protected void setCachedMessageRowDictionaryForFolder(NSDictionary<Integer, MessageRow> messageRows, String folderName) {
		if (MailSession.INBOX_FOLDER_NAME.equals(folderName)) {
			cachedInboxMessageRows = messageRows;
		} else {
			throw (new MailSessionException("Cannot set MessageRow objects for specified folderName as such a folder does not exist"));
		}
	}

	/**
	 * Sort the specified folder's message list and return the newly sorted list.
	 * Sort the specified folder's message list using specified sorting key; do
	 * a reverse sort if reverseSort is true.
	 *
	 * @param folderName Name of the folder whose messages will be sorted.
	 * @param sortKey Key representing which message property by which to sort.
	 * @param reverseSort Whether to reverse sort.
	 */
	public void sortMessageRowsForFolderSortedWithKey(String folderName, String sortKey, boolean reverseSort) throws MessagingException {
		String currentSortKey = sortKeyForFolder(folderName);
		boolean currentReverseSort = isReverseSortForFolder(folderName);

		// Set the new parameters:
		setSortKeyForFolder(sortKey, folderName);
		setReverseSortForFolder(reverseSort, folderName);

		if (!currentSortKey.equals(sortKey)) {
			// Sort key has changed, just invalidate cache
			cachedSortedInboxMessageNumbers = null;
			cachedSortedInboxMessageRows = null;
		} else if (reverseSort != currentReverseSort) {
			// Sort key hasn't changed; only reverse the order:
			cachedSortedInboxMessageNumbers = ERXArrayUtilities.reverse(cachedSortedInboxMessageNumbers);
			cachedSortedInboxMessageRows = ERXArrayUtilities.reverse(cachedSortedInboxMessageRows);
		}
	}

	private Map<String, String> folderNameToSortKeyMap = Collections.synchronizedMap(new HashMap<String, String>());
	private Map<String, Boolean> folderNameToReverseSortMap = Collections.synchronizedMap(new HashMap<String, Boolean>());

	public String sortKeyForFolder(String folderName) {
		String sortKey = (String)folderNameToSortKeyMap.get(folderName);

		if (sortKey == null) {
			// Set sort column to Date Sent as default:
			sortKey = MessageRow.DATE_SENT_SORT_FIELD;

			folderNameToSortKeyMap.put(folderName, sortKey);
		}

		return (sortKey);
	}

	protected void setSortKeyForFolder(String sortKey, String folderName) {
		folderNameToSortKeyMap.put(folderName, sortKey);
	}

	public boolean isReverseSortForFolder(String folderName) {
		Boolean isReverseSort = (Boolean)folderNameToReverseSortMap.get(folderName);

		if (isReverseSort == null) {
			// Set reverse to false as default:
			isReverseSort = Boolean.FALSE;

			folderNameToReverseSortMap.put(folderName, isReverseSort);
		}

		return (isReverseSort.booleanValue());
	}

	protected void setReverseSortForFolder(boolean reverse, String folderName) {
		folderNameToReverseSortMap.put(folderName, Boolean.valueOf(reverse));
	}

	// Messages
	public void moveMessageRowsToFolder(NSArray<MessageRow> messageRows, String folderName) throws MessagingException {
		if (!MailSession.TRASH_FOLDER_NAME.equals(folderName)) {
			throw (new MailSessionException("Can only move to Trash in POP3.  Not allowed to use folderName="+folderName));
		}

		moveMessageRowsToTrash(messageRows);
	}

	protected void moveMessageRowsToTrash(NSArray<MessageRow> messageRows) throws MessagingException {
		deleteMessageRows(messageRows);
	}

	synchronized protected void deleteMessageRows(NSArray<MessageRow> messageRows) throws MessagingException {
		Enumeration<MessageRow> en1 = messageRows.objectEnumerator();
		
		while (en1.hasMoreElements()) {
			deleteMessageRow(en1.nextElement());
		}
	}

	protected void deleteMessageRow(MessageRow messageRow) throws MessagingException {
		messageRow.setIsDeleted(true);

		cachedInboxMessageRows.remove(messageRow.getMessageNumber());
		cachedSortedInboxMessageNumbers.remove(messageRow.getMessageNumber());
		cachedSortedInboxMessageRows.remove(messageRow);
	}

}
