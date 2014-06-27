
package applications;

import java.awt.List;
import java.sql.Time;
import java.util.ArrayList;

import core.Application;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;

/**
 * Danger application to warn other hosts of a danger and to react when receiving
 * a warning message.
 * 
 * @author Virginie Collombon
 * @author David San
 */

public class DangerApplication extends Application {

	/** Application ID */
	public static final String APP_ID = "applications.DangerApplication";

	public static final String KEY_MESSAGE = "DANGER";
	
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
		if(msg.getProperty(KEY_MESSAGE) != null){
			host.setWarned(true);
		}
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
		if(host.isWarned()){
			for (Connection c : host.getConnections()) {
				DTNHost h = c.getOtherNode(host);
				Message m = new Message(host, h, "danger" + SimClock.getIntTime() + "-" + host.getAddress(), 10);
				m.addProperty(KEY_MESSAGE, true);
				m.setAppID(APP_ID);
				host.createNewMessage(m);
				super.sendEventToListeners("SentDanger", null, host);
			}
		}
	}
}
