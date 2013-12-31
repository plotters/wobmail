package net.xytra.wobmail.mailconn.message;

public class WobmailMessageUtils {
	/**
	 * @param key The key to test.
	 * @return true if <code>key</code> is one of the valid sorting keys
	 */
	public static boolean isSortKeyValid(String key) {
		return ((key != null) &&
				(WobmailMessage.DATE_SENT_SORT_FIELD.equals(key) ||
						WobmailMessage.SENDER_SORT_FIELD.equals(key) ||
						WobmailMessage.SUBJECT_SORT_FIELD.equals(key)));
	}

}
