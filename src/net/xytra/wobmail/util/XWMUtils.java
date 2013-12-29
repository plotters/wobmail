package net.xytra.wobmail.util;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import net.xytra.wobmail.export.ExportVisitor;
import net.xytra.wobmail.export.PartExportVisitor;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableData;

import er.javamail.ERMailUtils;

public class XWMUtils
{
	private static final String NEWLINE = "\n";

	public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
	public static final String CONTENT_TYPE_TEXT_HTML = "text/html";

	public static ExportVisitor exportVisitorForPart(Part part)
	{
		return (new PartExportVisitor(part));
	}

	public static NSData fullMimeMessageSource(MimeMessage message) throws IOException, MessagingException
	{
		NSMutableData content = new NSMutableData();

		Enumeration<String> en1 = message.getAllHeaderLines();
		while (en1.hasMoreElements())
		{
			content.appendBytes(((String)en1.nextElement()).getBytes());
			content.appendBytes(NEWLINE.getBytes());
		}

		content.appendBytes(NEWLINE.getBytes());

		int size = message.getSize();
		if (size < 1)
			size = 10240;

		content.appendData(new NSData(message.getRawInputStream(), size));

		return (content);
	}

	public static String quotedText(String originalText, Date date, String name, boolean isReply)
	{
		if (originalText == null)
			return (null);

		// TODO: localize
		StringBuffer sb = new StringBuffer();
		if (isReply)
			sb.append("\n\nOn ").append(date.toString()).append(", ").append(name).append(" wrote:\n> ");
		else
			sb.append("\n\nBegin forwarded message:\n> ");

		sb.append(originalText.replaceAll("\n", "\n> ")).append('\n');

		return (sb.toString());
	}

	public static String defaultStringContentForPart(Part part) throws MessagingException, IOException
	{
		Part p = defaultPartForPart(part);
		if (p == null)
			return (null);

		return ((String)p.getContent());
	}

	protected static Part defaultPartForPart(Part part) throws MessagingException, IOException
	{
		Object o = part.getContent();
		if (o instanceof Multipart)
			return (defaultPartForMultipart((Multipart)o));
		else if (o instanceof String)
			return (part);

		return (null);
	}

	protected static Part defaultPartForMultipart(Multipart multipart) throws MessagingException, IOException
	{
		Part p = null;
		int c = multipart.getCount();
		for (int i=0; (p == null || !isPartPlainText(p)) && i<c; i++)
			p = defaultPartForPart(multipart.getBodyPart(i));

		return (p);
	}

	protected static boolean isPartHTML(Part part) throws MessagingException
	{
		return (isPartOfContentType(part, CONTENT_TYPE_TEXT_HTML));
	}

	protected static boolean isPartPlainText(Part part) throws MessagingException
	{
		return (isPartOfContentType(part, CONTENT_TYPE_TEXT_PLAIN));
	}

	protected static boolean isPartOfContentType(Part part, String contentType) throws MessagingException
	{
		return (contentType.equals(part.getContentType().split(";")[0].toLowerCase()));
	}

	public static String ccAddressesAsStringForMessage(Message message) throws MessagingException
	{
		return (recipientAddressesAsStringForMessage(message, Message.RecipientType.CC));
	}

	public static String fromAddressesAsStringForMessage(Message message) throws MessagingException
	{
		return (addressesAsString(message.getFrom()));
	}

	public static String toAddressesAsStringForMessage(Message message) throws MessagingException
	{
		return (recipientAddressesAsStringForMessage(message, Message.RecipientType.TO));
	}

	public static String recipientAddressesAsStringForMessage(Message message, Message.RecipientType recipientType) throws MessagingException
	{
		return (addressesAsString(message.getRecipients(recipientType)));
	}

	protected static String addressesAsString(Address[] addresses)
	{
		if (addresses == null)
			return ("(Unspecified)");
		else if (addresses.length == 0)
			return ("(None)");

		Enumeration<Address> en1 = new NSArray<Address>(addresses).objectEnumerator();
		StringBuffer sb = new StringBuffer();
		sb.append(en1.nextElement().toString());

		while (en1.hasMoreElements())
			sb.append(", ").append(en1.nextElement().toString());

		return (sb.toString());
	}

	public static String internetAddressArrayToString(Message message, RecipientType recipientType)
		throws MessagingException
	{
		return (addressStringArrayToString(
				ERMailUtils.convertInternetAddressesToNSArray(
						message.getRecipients(recipientType))));
	}

	public static void setInternetAddressArrayForString(String s, Message message, RecipientType recipientType)
		throws AddressException, MessagingException
	{
		message.setRecipients(recipientType,
				ERMailUtils.convertNSArrayToInternetAddresses(
						stringToAddressStringArray(s)));
	}

	protected static String addressStringArrayToString(NSArray<String> array)
	{
		if (array.count() == 0)
			return (null);

		StringBuffer sb = new StringBuffer();
		Enumeration<String> en1 = array.objectEnumerator();
		sb.append(en1.nextElement());
		
		while (en1.hasMoreElements())
			sb.append(',').append(en1.nextElement());

		return (sb.toString());
	}

	protected static NSArray<String> stringToAddressStringArray(String s)
	{
		NSMutableArray<String> array = new NSMutableArray<String>();
		String[] addresses = s.split(",");
		for (int i=0; i<addresses.length; i++)
			array.addObject(addresses[i].trim());

		return (array);
	}

}
