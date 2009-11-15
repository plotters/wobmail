package net.xytra.wobmail.components;
// Generated by the WOLips Templateengine Plug-in at Apr 21, 2007 6:01:35 PM

import java.util.Enumeration;

import javax.mail.MessagingException;

import net.xytra.wobmail.application.Application;
import net.xytra.wobmail.manager.MailSession;
import net.xytra.wobmail.misc.MessageRow;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

import er.extensions.ERXArrayUtilities;

public class XWMList extends XWMAbstractPage
{
	public final NSArray numberPerPageArray = new NSArray(new Object[] { 2, 5, 10, 25, 50, 100 });

	public int currentMessageIndex;
	public MessageRow currentMessageRow;

	private NSArray<MessageRow> availableMessages = null;
	private NSArray<MessageRow> messageArrayForCurrentFolder = null;

	private boolean forceListReload = false;

	private Integer _currentEndIndex = null;
	private Integer _currentStartIndex = null;

	public XWMList(WOContext context) {
		super(context);
	}

	// Actions
	/**
	 * Process the new number of messages per page and return same page.
	 * Ensure selected page index is reset to the first page.
	 *
	 * @return same message list page.
	 */
	public WOComponent changeNumberPerPageAction()
	{
		// The new selected number of messages per page is already set through the dropdown.

		// Set the page index back to the first page
		session().selectedPageIndex = 0;

		// Flush the visible list of messages
		availableMessages = null;

		return (context().page());
	}

	public WOComponent moveToTrashSelectedMessagesAction() throws MessagingException
	{
		// Mark selected messages as deleted and return to List
		NSArray<MessageRow> selectedMessageRows = getSelectedMessageRows();
		if (selectedMessageRows.size() > 0) {
			session().getMailSession().moveMessageRowsToFolderWithName(
					selectedMessageRows, MailSession.TRASH_FOLDER_NAME);

			// Reset cached arrays
			availableMessages = null;
			messageArrayForCurrentFolder = null;
		}

		return (context().page());
	}

	/**
	 * Go to the first page of message list.
	 *
	 * @return same message list page.
	 */
	public WOComponent firstPageAction()
	{
		session().selectedPageIndex = 0;

		// Flush the visible list of messages
		availableMessages = null;

		return (context().page());
	}

	/**
	 * Go to the last page of message list.
	 *
	 * @return same message list page.
	 * @throws MessagingException 
	 */
	public WOComponent lastPageAction() throws MessagingException
	{
		session().selectedPageIndex = (messageArrayForCurrentFolder().size()-1) / session().selectedNumberPerPage;

		// Flush the visible list of messages
		availableMessages = null;

		return (context().page());
	}

	/**
	 * Go to the next page of message list.
	 *
	 * @return same message list page.
	 * @throws MessagingException 
	 */
	public WOComponent nextPageAction() throws MessagingException
	{
		session().selectedPageIndex = Math.min((messageArrayForCurrentFolder().size()-1) / session().selectedNumberPerPage,
				session().selectedPageIndex+1);

		// Flush the visible list of messages
		availableMessages = null;

		return (context().page());
	}

	/**
	 * Go to the previous page of message list.
	 *
	 * @return same message list page.
	 */
	public WOComponent previousPageAction()
	{
		session().selectedPageIndex = Math.max(0, session().selectedPageIndex-1);

		// Flush the visible list of messages
		availableMessages = null;

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

	protected NSArray<MessageRow> getSelectedMessageRows() throws MessagingException {
		NSMutableArray<MessageRow> selectedMessageRows = new NSMutableArray<MessageRow>();
		
		// For each message in folder, add to array if marked as selected
		Enumeration<MessageRow> en1 = messageArrayForCurrentFolder().objectEnumerator();

		while (en1.hasMoreElements()) {
			MessageRow mr = en1.nextElement();

			if (mr.isSelected()) {
				selectedMessageRows.addObject(mr);
			}
		}

		return (selectedMessageRows);
	}

	protected void markAllMessagesAsSelected(boolean selected) throws MessagingException {
		// Set all messages listed on page as selected or not
		Enumeration<MessageRow> en1 = getAvailableMessages().objectEnumerator();

		while (en1.hasMoreElements()) {
			en1.nextElement().setIsSelected(selected);
		}
	}

	public WOComponent sortByDateSentAction()
	{
		return (sortByFieldAction(MessageRow.DATE_SENT_SORT_FIELD));
	}

	public WOComponent sortBySenderAction()
	{
		return (sortByFieldAction(MessageRow.SENDER_SORT_FIELD));
	}

	public WOComponent sortBySubjectAction()
	{
		return (sortByFieldAction(MessageRow.SUBJECT_SORT_FIELD));
	}

	protected WOComponent sortByFieldAction(String fieldName)
	{
		if (fieldName == session().getCurrentSortField()) {
			session().setCurrentSortReverse(!session().getCurrentSortReverse());

			// If only reversing, we will lighten the load by just reversing the existing array:
			messageArrayForCurrentFolder = ERXArrayUtilities.reverse(messageArrayForCurrentFolder);
		} else {
			session().setCurrentSortField(fieldName);
			session().setCurrentSortReverse(false);

			messageArrayForCurrentFolder = null;
		}

		// Changing the sort field or direction invalidates our cached arrays:
		availableMessages = null;

		return (context().page());
	}

	public WOComponent viewMessageAction() throws MessagingException
	{
		XWMViewMessage page = (XWMViewMessage)pageWithName(XWMViewMessage.class.getName());
		page.setMessageFolderNameAndIndex(currentFolderName(), getCurrentMessageIndexInFullArray());

		return (page);
	}

	// Data
	public NSArray<MessageRow> getAvailableMessages() throws MessagingException
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
	// TODO: This would well be served by a caching mechanism
	protected NSArray<MessageRow> messageArrayForCurrentFolder() throws MessagingException {
		if (messageArrayForCurrentFolder == null) {
			NSArray<MessageRow> folderMessageRows = getMailSession().getMessageRowsForFolderWithName(MailSession.INBOX_FOLDER_NAME, getForceListReload());

			// Sort message rows according to session's parameters, if any is specified:
			if (session().getCurrentSortField() != null) {
				messageArrayForCurrentFolder = ERXArrayUtilities.sortedArraySortedWithKey(
						folderMessageRows,
						session().getCurrentSortField(),
						session().getCurrentSortReverse() ?
								EOSortOrdering.CompareCaseInsensitiveDescending :
								EOSortOrdering.CompareCaseInsensitiveAscending);
			} else {
				messageArrayForCurrentFolder = folderMessageRows;
			}
		}

		return (messageArrayForCurrentFolder);
	}

	protected NSArray<MessageRow> messageSubarrayForCurrentFolder(int startIndex, int endIndex) throws MessagingException {
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

	/**
	 * @return current folder name or localized version of "Inbox" if current folder is "INBOX".
	 */
	public String currentFolderName()
	{
		// TODO: Link this with current folder and localize if "INBOX"
		return ("Inbox");
	}

	public boolean showFirstAndPreviousLinks() {
		return (session().selectedPageIndex > 0);
	}

	public boolean showNextAndLastLinks() throws MessagingException {
		return (session().selectedPageIndex < ((messageArrayForCurrentFolder().size()-1) / session().selectedNumberPerPage));
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
			_currentStartIndex = session().selectedPageIndex * session().selectedNumberPerPage;
		}

		return (_currentStartIndex.intValue());
	}

	/**
	 * @return index of the message after the last one to be shown
	 * @throws MessagingException 
	 */
	protected int currentEndIndex() throws MessagingException {
		if (_currentEndIndex == null) {
			_currentEndIndex = Math.min(currentStartIndex() + session().selectedNumberPerPage, messageArrayForCurrentFolder().size());
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
		return (session().localizedDateTimeFormat().format(currentMessageRow.getDateSent()));
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
		return (currentMessageRow.isSelected());
	}

	public void setCurrentMessageChecked(boolean value) {
		currentMessageRow.setIsSelected(value);
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
