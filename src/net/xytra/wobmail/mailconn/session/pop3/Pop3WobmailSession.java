package net.xytra.wobmail.mailconn.session.pop3;

import javax.mail.Folder;
import javax.mail.MessagingException;

import net.xytra.wobmail.mailconn.WobmailException;
import net.xytra.wobmail.mailconn.WobmailStoreType;
import net.xytra.wobmail.mailconn.folder.WobmailFolder;
import net.xytra.wobmail.mailconn.folder.WobmailFolderType;
import net.xytra.wobmail.mailconn.folder.pop3.Pop3WobmailFolder;
import net.xytra.wobmail.mailconn.manager.Pop3WobmailSessionManager;
import net.xytra.wobmail.mailconn.session.AbstractWobmailSession;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;


public class Pop3WobmailSession extends AbstractWobmailSession
{
	private Folder inboxFolder = null;

	public Pop3WobmailSession(String username, String password) {
		super(username, password);
	}

	// Main stuff

	@Override
	protected WobmailStoreType getStoreType() {
		return (WobmailStoreType.pop3);
	}

	@Override
	public void deregisterForWOSessionID(String sessionID) {
		Pop3WobmailSessionManager.instance().deregisterEntry(sessionID)	;	
	}

	// Folders
	private NSArray<WobmailFolder> folders;

	@Override
	public NSArray<WobmailFolder> getFolders() {
		if (folders == null) {
			NSMutableArray<WobmailFolder> newFolders = new NSMutableArray<WobmailFolder>();

			try {
				Folder rootFolder = getOpenStore().getDefaultFolder();
				Folder[] topFolders = rootFolder.list();
				for (int i=0; i<topFolders.length; i++) {
					newFolders.addObject(new Pop3WobmailFolder(this, topFolders[i].getName()));
				}
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			folders = newFolders.immutableClone();
		}

		return folders;
	}

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
