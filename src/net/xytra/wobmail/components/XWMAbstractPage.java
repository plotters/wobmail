package net.xytra.wobmail.components;

import com.webobjects.appserver.WOContext;

public class XWMAbstractPage extends XWMAbstractNonSyncComponent
{
	public XWMAbstractPage(WOContext context) {
		super(context);
	}

	/**
	 * @return class name of page wrapper component, "XWMPageWrapper" by default.
	 */
	public String pageWrapperName() {
		return (XWMPageWrapper.class.getName());
	}

}
