package net.xytra.wobmail.components;

// Generated by the WOLips Templateengine Plug-in at Apr 18, 2007 9:01:26 PM

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import net.xytra.wobmail.application.Session;
import net.xytra.wobmail.manager.MailSessionException;
import net.xytra.wobmail.manager.Pop3MailSessionManager;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.ERXLocalizer;
import er.extensions.ERXLogger;
import er.extensions.ERXNonSynchronizingComponent;

public class Main extends ERXNonSynchronizingComponent
{
	public static final String INVALID_USER_PASS_ERROR_KEY = "Main.InvalidUsernameOrPassword";

	public String username;
	public String password;

	public String errorMessage;

	private ERXLocalizer localizer;

	public Main(WOContext context) {
		super(context);
	}

	public WOComponent loginAction()
	{
		errorMessage = null;
		Pop3MailSessionManager manager = Pop3MailSessionManager.instance();

		try {
			manager.registerMailSession((Session)session(), username, password);
		}
		catch (AuthenticationFailedException e) {
			errorMessage = getLocalizer().localizedStringForKeyWithDefault(INVALID_USER_PASS_ERROR_KEY);
			ERXLogger.log.debug(e);
		} catch (MessagingException me) {
			throw (new MailSessionException(me));
		}

		if (errorMessage != null)
			return (context().page());

		((Session)session()).setUsername(username);

		return (pageWithName(XWMList.class.getName()));
	}

	public ERXLocalizer getLocalizer() {
		if (localizer == null) {
			localizer = ERXLocalizer.defaultLocalizer();
		}

		return (localizer);
	}

}
