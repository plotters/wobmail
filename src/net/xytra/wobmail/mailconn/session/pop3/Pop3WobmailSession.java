package net.xytra.wobmail.mailconn.session.pop3;

import javax.mail.Folder;
import javax.mail.MessagingException;

import net.xytra.wobmail.mailconn.WobmailException;
import net.xytra.wobmail.mailconn.folder.WobmailFolder;
import net.xytra.wobmail.mailconn.folder.WobmailFolderType;
import net.xytra.wobmail.mailconn.folder.pop3.Pop3WobmailFolder;
import net.xytra.wobmail.mailconn.session.AbstractWobmailSession;

import com.webobjects.foundation.NSArray;


public class Pop3WobmailSession extends AbstractWobmailSession
{
	private Folder inboxFolder = null;

	public Pop3WobmailSession(String username, String password) {
		super(username, password);
	}

	// Folders
	public WobmailFolder getInboxFolder() {
		return (new Pop3WobmailFolder(this, WobmailFolderType.INBOX.name()));
	}

	// Underlying Folder operations
	public Folder obtainOpenFolder(String folderName) {
		try {
			return obtainOpenInboxFolder();
		} catch (MessagingException e) {
			throw (new WobmailException(e));
		}
	}

	synchronized protected Folder obtainOpenInboxFolder() throws MessagingException
	{
		if (inboxFolder == null) {
			inboxFolder = getOpenStore().getFolder(WobmailFolderType.INBOX.name());
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

}
