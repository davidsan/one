package applications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Application;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;

/**
 * Danger application to warn other hosts of a danger and to react when
 * receiving a warning message.
 * 
 * @author Virginie Collombon
 * @author David San
 */

public class DangerApplication extends Application {

	/** Message sending interval */
	public static final String SEND_INTERVAL = "interval";
	/** Message size */
	public static final String MESSAGE_SIZE = "size";

	/** Application ID */
	public static final String APP_ID = "applications.DangerApplication";

	/** Danger Key Message */
	public static final String DANGER_KEY_MESSAGE = "DANGER";

	// Private vars
	private Map<DTNHost, Double> hostDelayMap;
	// Message sending interval between two connected nodes
	private double sendInterval = 500;
	// Message size
	private int messageSize = 10;

	/**
	 * Creates a new danger application with the given settings.
	 * 
	 * @param s
	 *            Settings to use for initializing the application.
	 */
	public DangerApplication(Settings s) {
		if (s.contains(SEND_INTERVAL)) {
			this.sendInterval = s.getDouble(SEND_INTERVAL);
		}
		if (s.contains(MESSAGE_SIZE)) {
			this.sendInterval = s.getInt(MESSAGE_SIZE);
		}
		this.hostDelayMap = new HashMap<DTNHost, Double>();
		super.setAppID(APP_ID);
	}

	/**
	 * Copy-constructor
	 * 
	 * @param a
	 */
	public DangerApplication(DangerApplication a) {
		super(a);
		this.sendInterval = a.getSendInterval();
		this.messageSize = a.getMessageSize();
		this.hostDelayMap = new HashMap<DTNHost, Double>();
	}

	/**
	 * Handles an incoming message. If the message is a warning message sets the
	 * host warned boolean.
	 * 
	 * @param msg
	 *            message received by the router
	 * @param host
	 *            host to which the application instance is attached
	 */
	@Override
	public Message handle(Message msg, DTNHost host) {
		if (msg.getProperty(DANGER_KEY_MESSAGE) != null) {
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
	 * @param host
	 *            to which the application instance is attached
	 */
	@Override
	public void update(DTNHost host) {
		List<Connection> connections = host.getConnections();
		List<DTNHost> connectedHosts = new ArrayList<DTNHost>();
		List<DTNHost> disconnectedHosts = new ArrayList<DTNHost>();
		for (Connection connection : connections) {
			connectedHosts.add(connection.getOtherNode(host));
		}

		// remove disconnected host from hash
		// also remove host with expired timer
		for (DTNHost h : hostDelayMap.keySet()) {
			if (!connectedHosts.contains(h)
					|| SimClock.getTime() - hostDelayMap.get(h) >= sendInterval) {
				disconnectedHosts.add(h);
			}
		}

		for (DTNHost h : disconnectedHosts) {
			hostDelayMap.remove(h);
		}

		for (Connection c : connections) {
			DTNHost h = c.getOtherNode(host);
			if (!hostDelayMap.containsKey(h)) {
				System.err.println("Creation message de " + host.getAddress()
						+ " vers " + h.getAddress());
				hostDelayMap.put(h, SimClock.getTime());
				Message m = new Message(host, h, "danger"
						+ SimClock.getIntTime() + "-" + host.getAddress(),
						messageSize);

				if (host.isWarned()) {
					m.addProperty(DANGER_KEY_MESSAGE, true);
				}

				m.setAppID(APP_ID);
				host.createNewMessage(m);
				super.sendEventToListeners("SentDanger", null, host);

			}
		}
	}

	/**
	 * 
	 * @return message sending interval between two connected nodes
	 */
	public double getSendInterval() {
		return sendInterval;
	}

	/**
	 * 
	 * @return message size
	 */
	public int getMessageSize() {
		return messageSize;
	}
}
