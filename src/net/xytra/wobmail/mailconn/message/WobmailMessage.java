package net.xytra.wobmail.mailconn.message;

import java.util.Date;

import javax.mail.Message;

public interface WobmailMessage {
	public static final String DATE_SENT_SORT_FIELD = "dateSent";
	public static final String SENDER_SORT_FIELD = "sender"; 
	public static final String SUBJECT_SORT_FIELD = "subject";

	public Date getDateSent();

	/**
	 * TODO: can we avoid having this method in this interface?
	 * @return underlying <code>Message</code>
	 */
	public Message getMessage();

	public int getMessageNumber();

	public String getSender();

	public String getSubject();

	public void setIsDeleted(boolean value);

	public boolean isSelected();

	public void setIsSelected(boolean value);

}
