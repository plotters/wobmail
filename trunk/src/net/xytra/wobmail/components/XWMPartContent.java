package net.xytra.wobmail.components;
// Generated by the WOLips Templateengine Plug-in at Apr 22, 2007 2:10:52 AM

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimePart;

import net.xytra.wobmail.application.Session;
import net.xytra.wobmail.export.ExportVisitor;
import net.xytra.wobmail.util.XWMUtils;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

public class XWMPartContent extends ERXNonSynchronizingComponent
{
	private Part _part;

	public XWMPartContent(WOContext context) {
		super(context);
	}

//	public String filteredHTML() throws IOException, MessagingException
//	{
//		String content = (String)part().getContent();
//		content = content.replaceAll("<![Dd][Oo][Cc][Tt][Yy][Pp][Ee]\\W?.*>", "");
//		content = content.replaceAll("<\\s*/?\\s*[Hh][Tt][Mm][Ll]\\W?.*>", "");
//		content = content.replaceAll("<\\s*/?\\s*[Hh][Ee][Aa][Dd]\\W?.*>", "");
//		content = content.replaceAll("<\\s*[Tt][Ii][Tt][Ll][Ee]\\W?.*>.*<\\s*/\\s*[Tt][Ii][Tt][Ll][Ee]\\W?.*>", "");
//		content = content.replaceAll("<\\s*/?\\s*[Bb][Oo][Dd][Yy]\\W?.*>", "");
//		return (content);
//	}

	public String imageFileName() throws MessagingException {
		return (part().getFileName());
	}

	public String partUrl() throws IOException, MessagingException
	{
		int size = part().getSize();
		if (size < 1)
			size = 10240;

		ExportVisitor ev = XWMUtils.exportVisitorForPart(part());
		int index = ((Session)session()).registerDownloadbleObject(ev);

		return (((Session)session()).urlForViewableObject(index));
	}

	public boolean isMimePart() throws IOException, MessagingException {
		return (part().getContent() instanceof MimePart);
	}

	public MimePart partContent() throws IOException, MessagingException {
		return ((MimePart)part().getContent());
	}

	public String partInfo() throws MessagingException
	{
		return (shortContentPart());
	}

	protected Part part()
	{
		if (_part == null)
			_part = (Part)objectValueForBinding("part");

		return (_part);
	}

	protected String shortContentPart() throws MessagingException
	{
		return (part().getContentType().split(";")[0]);
	}

}
