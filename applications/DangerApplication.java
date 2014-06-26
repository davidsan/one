
package applications;

import core.Application;
import core.DTNHost;
import core.Message;
import core.Settings;

/**
 * Danger application to warn other hosts of a danger and to react when receiving
 * a warning message.
 * 
 * @author Virginie Collombon, David San
 */

public class DangerApplication extends Application {

	/** Application ID */
	public static final String APP_ID = "applications.DangerApplication";

	
	/** 
	 * Creates a new danger application with the given settings.
	 * @param s	Settings to use for initializing the application.
	 */
	public DangerApplication(Settings s) {
		super.setAppID(APP_ID);
	}
	
	/** 
	 * Copy-constructor
	 * @param a
	 */
	public DangerApplication(DangerApplication a) {
		super(a);
	}
	
	/** 
	 * Handles an incoming message. If the message is a warning message 
	 * sets the host warned boolean.
	 * 
	 * @param msg	message received by the router
	 * @param host	host to which the application instance is attached
	 */
	@Override
	public Message handle(Message msg, DTNHost host) {
		// TODO
		return msg;
	}

	@Override
	public Application replicate() {
		return new DangerApplication(this);
	}

	/** 
	 * Creates warning message for other hosts in range
	 * 
	 * @param host to which the application instance is attached
	 */
	@Override
	public void update(DTNHost host) {
		// TODO
	}
	
}
