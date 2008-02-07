package net.xytra.wobmail.application;
// Generated by the WOLips Templateengine Plug-in at Apr 18, 2007 9:01:26 PM

import java.util.Enumeration;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import net.xytra.wobmail.export.ExportVisitor;
import net.xytra.wobmail.manager.Pop3SessionManager;
import net.xytra.wobmail.misc.MessageRow;
import net.xytra.wobmail.util.XWMUtils;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXArrayUtilities;
import er.extensions.ERXConstant;
import er.extensions.ERXSession;

public class Session extends ERXSession
{
	private String username;

	private NSMutableArray downloadableObjects = new NSMutableArray();
	private NSMutableArray selectedMessagesForDeletion = new NSMutableArray();

	private NSArray _availableInboxMessageRows;
	private NSArray _availableInboxMessages;

	public void terminate()
	{
		Pop3SessionManager.instance().deregisterEntry(sessionID());
		super.terminate();
	}

	public String username() {
		return (this.username);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public ExportVisitor getDownloadableObject(int index) {
		return ((ExportVisitor) downloadableObjects.objectAtIndex(index));
	}

	public int registerDownloadbleObject(ExportVisitor object)
	{
		int index;

		synchronized (this.downloadableObjects)
		{
			this.downloadableObjects.addObject(object);
			index = this.downloadableObjects.count() - 1;
		}

		return (index);
	}

	public void clearDownloadableObjects() {
		downloadableObjects.removeAllObjects();
	}

	public String urlForDownloadableObject(int index)
	{
		return (urlForExportableObject(index, "downloadObject"));
	}

	public String urlForViewableObject(int index)
	{
		return (urlForExportableObject(index, "viewObject"));
	}

	protected String urlForExportableObject(int index, String directActionName)
	{
		return (context().directActionURLForActionNamed(
				directActionName,
				new NSDictionary(
						new Object[] { sessionID(), Integer.toString(index) },
						new String[] { "wosid", "id" })));
	}

	public static final String MSG_NUM_PARAM = "number";
	public static final String MSG_DATE_PARAM = "dateSent";
	public static final String MSG_SENDER_PARAM = "sender";
	public static final String MSG_SUBJECT_PARAM = "subject";

	private String currentSortParam = null;
	private boolean currentSortReverse = false;

	public NSArray availableInboxMessageRows(boolean forceReload, String sortParam, boolean sortReverse) throws MessagingException
	{
		if (forceReload || (_availableInboxMessageRows == null))
		{
			Folder folder = Pop3SessionManager.instance().obtainOpenInboxFor(sessionID());
			Message[] messages = folder.getMessages();
			NSMutableArray nonDeletedMessages = new NSMutableArray();
			for (int i=0; i<messages.length; i++)
			{
				Message m = messages[i];
				if (!m.isSet(Flags.Flag.DELETED))
					nonDeletedMessages.addObject(messageRowForMessage(m));
			}

			_availableInboxMessageRows = nonDeletedMessages;
		}

		if (sortParam != null)
		{
			if (sortParam != currentSortParam)
			{
				_availableInboxMessageRows = ERXArrayUtilities.sortedArraySortedWithKey(_availableInboxMessageRows, sortParam);
				if (sortReverse)
					_availableInboxMessageRows = ERXArrayUtilities.reverse(_availableInboxMessageRows);

				this.currentSortParam = sortParam;
				this.currentSortReverse = sortReverse;
			}
			else
			{
				if (sortReverse != currentSortReverse)
				{
					_availableInboxMessageRows = ERXArrayUtilities.reverse(_availableInboxMessageRows);
					this.currentSortReverse = sortReverse;
				}
			}
		}

		return (_availableInboxMessageRows);
	}

	private MessageRow messageRowForMessage(Message m) throws MessagingException
	{
		return (new MessageRow(
				m.getMessageNumber(),
				m.getSentDate(),
				XWMUtils.fromAddressesAsStringForMessage(m),
				m.getSubject()));
	}

	public NSArray availableInboxMessages() throws MessagingException
	{
		if (_availableInboxMessages == null)
		{
			Folder folder = Pop3SessionManager.instance().obtainOpenInboxFor(sessionID());
			Message[] messages = folder.getMessages();
			NSMutableArray nonDeletedMessages = new NSMutableArray();
			for (int i=0; i<messages.length; i++)
			{
				Message m = messages[i];
				if (!m.isSet(Flags.Flag.DELETED))
					nonDeletedMessages.addObject(m);
			}

			_availableInboxMessages = nonDeletedMessages;
		}

		return (_availableInboxMessages);
	}

	public void deleteSelectedMessages() throws MessagingException
	{
		if (selectedMessagesForDeletion.count() == 0)
			return;

		Enumeration en1 = selectedMessagesForDeletion.objectEnumerator();
		while (en1.hasMoreElements())
			((Message)availableInboxMessages().objectAtIndex(((Number)en1.nextElement()).intValue())).setFlag(Flags.Flag.DELETED, true);

		// Force a list reset
		resetAvailableInboxMessages();
	}

	public boolean isMessageSelectedForDeletion(int index)
	{
		return (this.selectedMessagesForDeletion.containsObject(ERXConstant.integerForInt(index)));
	}

	public void setMessageSelectedForDeletion(int index, boolean select)
	{
		if (select)
			this.selectedMessagesForDeletion.addObject(ERXConstant.integerForInt(index));
		else
			this.selectedMessagesForDeletion.removeObject(ERXConstant.integerForInt(index));
	}

	public void setSelectedAllMessagesForDeletion(boolean selected) throws MessagingException
	{
		this.selectedMessagesForDeletion.removeAllObjects();

		if (selected)
		{
			int c = availableInboxMessages().count();

			for (int i=0; i<c; i++)
				setMessageSelectedForDeletion(i, true);
		}
	}

	public void resetAvailableInboxMessages()
	{
		this._availableInboxMessages = null;
		this.selectedMessagesForDeletion.removeAllObjects();
	}

}
