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
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;


public class Pop3WobmailSession extends AbstractWobmailSession
{
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
	// Much of the following can probably go in AbstractWobmailSession
	private NSArray<WobmailFolder> folders;
	private NSDictionary<String, WobmailFolder> foldersDict;

	public NSArray<WobmailFolder> getFolders() {
		// Keep it simple for now
		return (getFoldersInternal());
	}

	private NSArray<WobmailFolder> getFoldersInternal() {
		if (folders == null) {
			NSMutableArray<WobmailFolder> newFolders = new NSMutableArray<WobmailFolder>();
			NSMutableDictionary<String, WobmailFolder> newFoldersDict = new NSMutableDictionary<String, WobmailFolder>();

			try {
				Folder rootFolder = getOpenStore().getDefaultFolder();
				Folder[] topFolders = rootFolder.list();
				for (int i=0; i<topFolders.length; i++) {
					WobmailFolder wFolder = new Pop3WobmailFolder(this, topFolders[i].getFullName()); 
					newFolders.addObject(wFolder);
					newFoldersDict.setObjectForKey(wFolder, topFolders[i].getName());
				}
			} catch (MessagingException e) {
				throw (new WobmailException(e));
			}
			
			folders = newFolders.immutableClone();
			foldersDict = newFoldersDict.immutableClone();
		}

		return folders;
	}

	@Override
	public WobmailFolder getInboxFolder() {
		// TODO: make this a bit more elegant
		getFoldersInternal();

//		return (new Pop3WobmailFolder(this, WobmailFolderType.INBOX.name()));
		return (foldersDict.objectForKey(WobmailFolderType.INBOX.name()));
	}

	// Underlying Folder operations
	private NSMutableDictionary<String, Folder> openFolders = new NSMutableDictionary<String, Folder>();

	public Folder obtainOpenFolderByFullName(String folderFullName) {
		Folder folder = null;

		// First, get folder by name (and cache it):
		synchronized (openFolders) {
			folder = openFolders.objectForKey(folderFullName);

			if (folder == null) {
				try {
					folder = getOpenStore().getFolder(folderFullName);
				} catch (MessagingException e) {
					throw (new WobmailException(e));
				}
				openFolders.setObjectForKey(folder, folderFullName);
			}
		}

		// Then ensure it is open:
		try {
			return (obtainOpenFolder(folder));
		} catch (MessagingException e) {
			throw (new WobmailException(e));
		}
	}

	@Override
	protected void forgetOpenFolders() {
		openFolders.removeAllObjects();
	}

	@Override
	protected NSArray<Folder> getOpenFolders() {
		return (openFolders.allValues());
	}

}
