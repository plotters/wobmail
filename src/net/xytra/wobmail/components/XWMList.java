package net.xytra.wobmail.components;
// Generated by the WOLips Templateengine Plug-in at Apr 21, 2007 6:01:35 PM

import java.util.Enumeration;

import javax.mail.MessagingException;

import net.xytra.wobmail.application.Application;
import net.xytra.wobmail.mailconn.message.WobmailMessage;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

import er.extensions.logging.ERXLogger;

public class XWMList extends XWMAbstractPage
{
	protected static enum SelectionType {
		ALL,
		NONE;
	}

	public final NSArray<Integer> numberPerPageArray = new NSArray<Integer>(new Integer[] { 2, 5, 10, 25, 50, 100 });

	public int currentMessageIndex;
	public WobmailMessage currentMessage;

	private NSArray<WobmailMessage> availableMessages = null;
	private NSArray<WobmailMessage> messageArrayForCurrentFolder = null;

	private boolean forceListReload = false;

	public int currentNumberPerPage;

	private Integer _currentEndIndex = null;
	private Integer _currentStartIndex = null;

	public XWMList(WOContext context) {
		super(context);
	}

	// Actions
	public WOComponent moveToTrashSelectedMessagesAction() throws MessagingException
	{
		// Mark selected messages as deleted and return to List
		NSArray<WobmailMessage> selectedMessages = getSelectedMessages();
		if (selectedMessages.size() > 0) {
			getActiveFolder().trashMessages(selectedMessages);

			// Reset cached arrays
			availableMessages = null;
			messageArrayForCurrentFolder = null;
		}

		return (context().page());
	}

	public WOComponent selectAllMessagesAction() throws MessagingException
	{
		// Set all messages listed on page as selected
		markAllMessagesAsSelected(true);

		return (context().page());
	}

	public WOComponent selectNoMessagesAction() throws MessagingException
	{
		// Set all messages listed on page as unselected
		markAllMessagesAsSelected(false);

		return (context().page());
	}

	// Supporting methods
	public String checkedStringIfCurrentMessageSelected() {
		return (currentMessage.isSelected() ? "checked" : "");
	}

	protected NSArray<WobmailMessage> getSelectedMessages() throws MessagingException {
		NSMutableArray<WobmailMessage> selectedMessageRows = new NSMutableArray<WobmailMessage>();
		
		// For each message in folder, add to array if marked as selected
		Enumeration<WobmailMessage> en1 = messageArrayForCurrentFolder().objectEnumerator();

		while (en1.hasMoreElements()) {
			WobmailMessage message = en1.nextElement();

			if (message.isSelected()) {
				selectedMessageRows.addObject(message);
			}
		}

		return (selectedMessageRows);
	}

	/**
	 * Set messages as selected as per specified type.
	 * If selectionType is <code>null</code>, do nothing.  If not one of the
	 * values of the SelectionType enum, do nothing.
	 *
	 * @param selectionType ALL or NONE
	 * @throws MessagingException
	 */
	public void setMessagesAsSelected(String selectionType) throws MessagingException {
		if (selectionType == null) {
			return;
		}

		try {
			setMessageAsSelected(SelectionType.valueOf(selectionType));
		} catch (IllegalArgumentException e) {
			// Bad SelectionType name means we can just ignore this selection type
			ERXLogger.log.debug("Invalid selection type used for list: " + selectionType);
		}
	}

	protected void setMessageAsSelected(SelectionType selectionType) throws MessagingException {
		markAllMessagesAsSelected(selectionType == SelectionType.ALL);
	}

	protected void markAllMessagesAsSelected(boolean selected) throws MessagingException {
		// Set all messages listed on page as selected or not
		Enumeration<WobmailMessage> en1 = getAvailableMessages().objectEnumerator();

		while (en1.hasMoreElements()) {
			en1.nextElement().setIsSelected(selected);
		}
	}

	// Data
	public NSArray<WobmailMessage> getAvailableMessages() throws MessagingException
	{
		if (availableMessages == null) {
			availableMessages = messageSubarrayForCurrentFolder(currentStartIndex(), currentEndIndex());
		}

		return (availableMessages);
	}

	/**
	 * @return the array representing the full list of messages in this folder.
	 * @throws MessagingException
	 */
	protected NSArray<WobmailMessage> messageArrayForCurrentFolder() throws MessagingException {
		if (messageArrayForCurrentFolder == null) {
			messageArrayForCurrentFolder = getActiveFolder().getMessages(getForceListReload());
		}

		return (messageArrayForCurrentFolder);
	}

	protected NSArray<WobmailMessage> messageSubarrayForCurrentFolder(int startIndex, int endIndex) throws MessagingException {
		return (messageArrayForCurrentFolder().subarrayWithRange(new NSRange(startIndex, endIndex-startIndex)));
	}

	public String listRowClass() throws MessagingException
	{
		StringBuffer sb = new StringBuffer();

		if ((currentMessageIndex % 2) == 0)
			sb.append("XWMlistRowLight");
		else
			sb.append("XWMlistRowDark");

		return (sb.toString());
	}

	public String listScriptUrl() {
		return (Application.application().resourceManager().urlForResourceNamed("mailList.js", "app", null, context().request()));
	}

	// Sorting
	public String sortKeyForDateSent() {
		return (WobmailMessage.DATE_SENT_SORT_FIELD);
	}

	public int reverseNextSortForDateSentAsInt() {
		return (reverseNextSortForKeyAsInt(sortKeyForDateSent()));
	}

	public String sortKeyForSender() {
		return (WobmailMessage.SENDER_SORT_FIELD);
	}

	public int reverseNextSortForSenderAsInt() {
		return (reverseNextSortForKeyAsInt(sortKeyForSender()));
	}

	public String sortKeyForSubject() {
		return (WobmailMessage.SUBJECT_SORT_FIELD);
	}

	public int reverseNextSortForSubjectAsInt() {
		return (reverseNextSortForKeyAsInt(sortKeyForSubject()));
	}

	protected int reverseNextSortForKeyAsInt(String sortKey) {
		return (!session().getCurrentSortReverse() &&
				sortKey.equals(session().getCurrentSortField()) ? 1 : 0);
	}

	public boolean showFirstAndPreviousLinks() {
		return (session().getSelectedPageIndex() > 0);
	}

	public boolean showNextAndLastLinks() throws MessagingException {
		return (session().getSelectedPageIndex() < ((messageArrayForCurrentFolder().size()-1) / session().getSelectedNumberPerPage()));
	}

	// Methods to access the current list bounds
	public int currentPrintableStartIndex() {
		return (currentStartIndex() + 1);
	}

	public int currentPrintableEndIndex() throws MessagingException {
		return (currentEndIndex());
	}

	public int currentPrintableTotalMessages() throws MessagingException {
		return (messageArrayForCurrentFolder().size());
	}

	/**
	 * @return index of first message to be shown, starting at zero
	 */
	protected int currentStartIndex() {
		if (_currentStartIndex == null) {
			_currentStartIndex = session().getSelectedPageIndex() * session().getSelectedNumberPerPage();
		}

		return (_currentStartIndex.intValue());
	}

	/**
	 * @return index of the message after the last one to be shown
	 * @throws MessagingException 
	 */
	protected int currentEndIndex() throws MessagingException {
		if (_currentEndIndex == null) {
			_currentEndIndex = Math.min(currentStartIndex() + session().getSelectedNumberPerPage(), messageArrayForCurrentFolder().size());
		}

		return (_currentEndIndex.intValue());
	}

	/**
	 * @return the index of the current message within the full array of MessageRows.
	 */
	public int getCurrentMessageIndexInFullArray() {
		return (currentStartIndex() + currentMessageIndex);
	}

	public String presentableDateSent() throws MessagingException {
		return (session().localizedDateTimeFormat().format(currentMessage.getDateSent()));
	}

	// Page indices
	/**
	 * @return the index of the last page of messages.
	 * @throws MessagingException
	 */
	public int getLastPageIndex() throws MessagingException {
		return ((messageArrayForCurrentFolder().size()-1) / session().getSelectedNumberPerPage());
	}

	/**
	 * @return the index of the next page of messages, regardless if it exists.
	 */
	public int getNextPageIndex() {
		return (session().getSelectedPageIndex() + 1);
	}

	/**
	 * @return the index of the previous page of messages, regardless if it exists.
	 */
	public int getPreviousPageIndex() {
		return (session().getSelectedPageIndex() - 1);
	}

	// Internal switches
	protected boolean getForceListReload() {
		return (forceListReload);
	}

	public void setForceListReload(boolean value) {
		forceListReload = value;
	}

	// Check boxes
	public boolean currentMessageChecked() {
		return (currentMessage.isSelected());
	}

	public void setCurrentMessageChecked(boolean value) {
		currentMessage.setIsSelected(value);
	}

	/**
	 * @see com.webobjects.appserver.WOComponent#appendToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
	 */
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		// Before we start building the response of this page, reset a few cached things...
		// We have to cache these (and then reset them in here) because
		// otherwise the page uses the new num per page too early.
		_currentStartIndex = null;
		_currentEndIndex = null;

		super.appendToResponse(response, context);
	}

}
