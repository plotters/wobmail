package net.xytra.wobmail.components;
// Generated by the WOLips Templateengine Plug-in at Apr 22, 2007 1:40:52 AM

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import com.webobjects.appserver.WOContext;

import er.extensions.ERXNonSynchronizingComponent;

public class XWMContent extends ERXNonSynchronizingComponent
{
	public XWMContent(WOContext context) {
		super(context);
	}

	public boolean isMultipart() throws IOException, MessagingException {
		return (((Part)objectValueForBinding("message")).getContent() instanceof Multipart);
	}

}
