package net.xytra.wobmail.components;

import net.xytra.wobmail.mailconn.folder.WobmailFolder;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

public class XWMFolderPane extends XWMAbstractNonSyncComponent {
	public XWMFolderPane(WOContext context) {
		super(context);
	}

	public WobmailFolder currentFolder;

	public NSArray<WobmailFolder> getAvailableFolders() {
		return (getWobmailSession().getFolders());
	}

	public boolean isCurrentFolderActive() {
		return (currentFolder.equals(getActiveFolder()));
	}

}
