package net.xytra.wobmail.application;
// Generated by the WOLips Templateengine Plug-in at Apr 18, 2007 9:01:26 PM

import com.webobjects.foundation.NSLog;

import er.extensions.ERXApplication;
import er.extensions.ERXProperties;

public class Application extends ERXApplication
{
	private String defaultIncomingMailServerAddress;

	public static void main(String argv[]) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		super();
		NSLog.out.appendln("Welcome to " + this.name() + " !");
		/* ** put your initialization code in here ** */
	}

	public String getDefaultIncomingMailServerAddress()
	{
		if (this.defaultIncomingMailServerAddress == null)
		{
			this.defaultIncomingMailServerAddress = ERXProperties.stringForKeyWithDefault(
					"net.xytra.wobmail.DefaultIncomingMailServerAddress",
					"localhost");
		}

		return (this.defaultIncomingMailServerAddress);
	}

}
