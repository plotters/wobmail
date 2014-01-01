package net.xytra.wobmail.components;

import net.xytra.wobmail.application.Session;
import net.xytra.wobmail.mailconn.folder.WobmailFolder;
import net.xytra.wobmail.mailconn.session.WobmailSession;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

public class XWMAbstractNonSyncComponent extends ERXNonSynchronizingComponent
{
	public XWMAbstractNonSyncComponent(WOContext context) {
		super(context);
	}

	/**
	 * @see com.webobjects.appserver.WOComponent#session()
	 * @return the session but as Session.
	 */
	@Override
	public Session session() {
		return ((Session)super.session());
	}

	// TODO: this probably has to be improved
	public WobmailFolder getActiveFolder() {
		return (session().getCurrentFolder());
	}

	/**
	 * Convenience method to get the WobmailSession.
	 * @return the WO session's associated WobmailSession.
	 */
	protected WobmailSession getWobmailSession() {
		return session().getWobmailSession();
	}

}
