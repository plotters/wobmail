package net.xytra.wobmail.components;
// Generated by the WOLips Templateengine Plug-in at Apr 21, 2007 9:51:26 PM

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import net.xytra.wobmail.application.Session;
import net.xytra.wobmail.export.ExportVisitor;
import net.xytra.wobmail.manager.MailSession;
import net.xytra.wobmail.misc.MessageRow;
import net.xytra.wobmail.util.XWMUtils;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXConstant;

public class XWMViewMessage extends XWMAbstractPage
{
	private Integer messageIndex;
	private MessageRow messageRow;

	public XWMViewMessage(WOContext context)
	{
		super(context);
		session().clearDownloadableObjects();
	}

	// Actions
	public WOComponent moveToTrashAction() throws MessagingException
	{
		// Mark message as deleted and return to List
		session().getMailSession().moveMessageRowToFolderWithName(
				getMessageRow(), MailSession.TRASH_FOLDER_NAME);

		return (pageWithName(XWMList.class.getName()));
	}

	public WOComponent forwardAction() throws MessagingException, IOException
	{
		XWMCompose page = (XWMCompose)pageWithName(XWMCompose.class.getName());
		page.setConstituentMessage(getMailSession().obtainNewMimeMessage());
		page.setSubject("Fwd: " + getMessage().getSubject());
		page.setEmailText(XWMUtils.quotedText(
				XWMUtils.defaultStringContentForPart(getMessage()),
				getMessage().getSentDate(),
				XWMUtils.fromAddressesAsStringForMessage(getMessage()),
				false));
		page.propagateAddresses();

		return (page);
	}

	public WOComponent forwardAsAttachmentAction() throws MessagingException
	{
		XWMCompose page = (XWMCompose)pageWithName(XWMCompose.class.getName());
		// TODO: check if type really matches
		page.attachMimeMessage((MimeMessage)getMessage());

		return (page);
	}

	public WOComponent replyAction() throws MessagingException, IOException
	{
		return (replyAction(false));
	}

	protected WOComponent replyAction(boolean replyToAll) throws MessagingException, IOException
	{
		XWMCompose page = (XWMCompose)pageWithName(XWMCompose.class.getName());
		page.setConstituentMessage((MimeMessage)getMessage().reply(replyToAll));
		page.setEmailText(XWMUtils.quotedText(
				XWMUtils.defaultStringContentForPart(getMessage()),
				getMessage().getSentDate(),
				XWMUtils.fromAddressesAsStringForMessage(getMessage()),
				true));
		page.propagateAddresses();

		return (page);
	}

	public WOComponent replyToAllAction() throws MessagingException, IOException
	{
		return (replyAction(true));
	}

	// Data
	/**
	 * @return true if link for next message in same folder should be shown, false otherwise.
	 * @throws MessagingException
	 */
	public boolean showNextMessageLink() throws MessagingException {
		int numMessagesInFolder = getMailSession().getNumberMessagesInFolderWithName(getMessageFolderName());

		return (getMessageIndex() < numMessagesInFolder-1);
	}

	/**
	 * @return true if link for previous message in same folder should be shown, false otherwise.
	 */
	public boolean showPreviousMessageLink() {
		return (getMessageIndex() > 0);
	}

	protected Message getMessage() throws MessagingException {
		return (getMessageRow().getMessage());
	}

	/**
	 * @return the MessageRow corresponding to the displayed Message.
	 * @throws MessagingException 
	 */
	public MessageRow getMessageRow() throws MessagingException {
		if (messageRow == null) {
			System.err.println("messageIndex="+messageIndex);
			messageRow = getMailSession().getMessageRowForFolderWithName(getMessageIndex(), getMessageFolderName());

			// Ensure connection is still open and folder too:
			getMailSession().keepConnectionOpenForMessage(messageRow.getMessage());
		}

		return (messageRow);
	}

	/**
	 * @return the folder name of the folder in which to find the message with the index specified earlier.
	 */
	protected String getMessageFolderName() {
		return (session().getCurrentFolderName());
	}

	public void setMessageFolderName(String newName) {
		session().setCurrentFolderName(newName);
	}

	/**
	 * @return the index of message as passed in earlier.
	 */
	protected int getMessageIndex() {
		return (messageIndex.intValue());
	}

	public void setMessageIndex(int index) {
		messageIndex = ERXConstant.integerForInt(index);
	}

	public int getNextMessageIndex() {
		return (getMessageIndex() + 1);
	}

	public int getPreviousMessageIndex() {
		return (getMessageIndex() - 1);
	}

	public String defaultMessageContent() throws MessagingException, IOException
	{
		return (XWMUtils.defaultStringContentForPart(getMessage()));
	}

	public String messageSender() throws MessagingException {
		return (XWMUtils.fromAddressesAsStringForMessage(getMessage()));
	}

	public String messageToRecipient() throws MessagingException {
		return (XWMUtils.toAddressesAsStringForMessage(getMessage()));
	}

	public String messageSubject() throws MessagingException {
		return (getMessageRow().getSubject());
	}

	public String presentableDateSent() throws MessagingException {
		return (session().localizedDateTimeFormat().format(getMessageRow().getDateSent()));
	}

	public String viewSourceUrl()
	{
		ExportVisitor ev = new ExportVisitor()
		{
			public NSData getFileContent()
			{
				try
				{
					return (XWMUtils.fullMimeMessageSource((MimeMessage)getMessage()));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (MessagingException e)
				{
					e.printStackTrace();
				}
				return (NSData.EmptyData);
			}

			public String getFileName()
			{
				return ("email-" + new NSTimestamp().getTime() + ".txt");
			}

			public String getFileType()
			{
				return (XWMUtils.CONTENT_TYPE_TEXT_PLAIN);
			}
		};

		int index = ((Session)session()).registerDownloadbleObject(ev);
		return (((Session)session()).urlForViewableObject(index));
	}

}
