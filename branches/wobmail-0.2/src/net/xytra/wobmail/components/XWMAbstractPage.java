package net.xytra.wobmail.components;

import net.xytra.wobmail.application.Session;
import net.xytra.wobmail.manager.MailSession;

import com.webobjects.appserver.WOContext;

import er.extensions.ERXNonSynchronizingComponent;

public class XWMAbstractPage extends ERXNonSynchronizingComponent
{
	public XWMAbstractPage(WOContext context) {
		super(context);
	}

	/**
	 * @return class name of page wrapper component, "XWMPageWrapper" by default.
	 */
	public String pageWrapperName() {
		return (XWMPageWrapper.class.getName());
	}

	/**
	 * Convenience method to get the MailSession.
	 * @return the WO session's associated MailSession.
	 */
	protected MailSession getMailSession() {
		return session().getMailSession();
	}

	/**
	 * @see com.webobjects.appserver.WOComponent#session()
	 * @return the session but as Session.
	 */
	@Override
	public Session session() {
		return ((Session)super.session());
	}

}