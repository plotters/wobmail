package net.xytra.wobmail.mailconn.folder;

public enum WobmailFolderType {
	INBOX("Inbox"),
	TRASH("Trash");

	private final String defaultVisibleName;

	private WobmailFolderType(String defaultName) {
		this.defaultVisibleName = defaultName;
	}

	public String defaultVisibleName() {
		return (defaultVisibleName);
	}
}
