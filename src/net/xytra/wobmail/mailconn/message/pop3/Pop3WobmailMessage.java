package net.xytra.wobmail.mailconn.message.pop3;

import java.util.Date;

import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;

import net.xytra.wobmail.mailconn.WobmailException;
import net.xytra.wobmail.mailconn.message.WobmailMessage;
import net.xytra.wobmail.util.XWMUtils;

import com.webobjects.foundation.NSKeyValueCoding;

public class Pop3WobmailMessage implements WobmailMessage, NSKeyValueCoding
{
	private Message message;

	private Date dateSent;
	private Boolean isDeleted;
	private Number messageNumber;
	private String sender;
	private String subject;

	private boolean isSelected = false;

	public Pop3WobmailMessage(Message message) {
		this.message = message;
	}

	@Override
	public Date getDateSent() {
		if (dateSent == null) {
			try {
				dateSent = message.getSentDate();
			} catch (MessagingException e) {
				throw (new WobmailException(e));
			}
		}

		return (dateSent);
	}

	@Override
	public Message getMessage() {
		return (message);
	}

	@Override
	public int getMessageNumber() {
		if (messageNumber == null) {
			messageNumber = Integer.valueOf(message.getMessageNumber());
		}

		return (messageNumber.intValue());
	}

	@Override
	public String getSender() {
		if (sender == null) {
			try {
				sender = XWMUtils.fromAddressesAsStringForMessage(message);
			} catch (MessagingException e) {
				throw (new WobmailException(e));
			}
		}

		return (sender);
	}

	@Override
	public String getSubject() {
		if (subject == null) {
			try {
				subject = message.getSubject();
			} catch (MessagingException e) {
				throw (new WobmailException(e));
			}
		}

		return (subject);
	}

	public boolean isDeleted() throws MessagingException {
		if (isDeleted == null) {
			isDeleted = Boolean.valueOf(message.isSet(Flag.DELETED));
		}

		return (isDeleted.booleanValue());
	}

	public void setIsDeleted(boolean value) {
		try {
			message.setFlag(Flag.DELETED, value);
		} catch (MessagingException e) {
			throw (new WobmailException(e));
		}

		// Update the cached version
		isDeleted = Boolean.valueOf(value);
	}

	@Override
	public boolean isSelected() {
		return (isSelected);
	}

	@Override
	public void setIsSelected(boolean value) {
		isSelected = value;
	}

	// NSKeyValueCoding stuff
	public void takeValueForKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
	}

	public Object valueForKey(String key) {
		return (NSKeyValueCoding.DefaultImplementation.valueForKey(this, key));
	}

}
