package applications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import movement.DangerMovement;
import movement.map.MapNode;
import util.Tuple;
import core.Application;
import core.Connection;
import core.Coord;
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

	/** Known Location Key Message */
	public static final String KNOWN_LOCATIONS_KEY_MESSAGE = "KNOWN_LOCATIONS";

	/** Known Accidents Key Message */
	public static final String KNOWN_ACCIDENTS_KEY_MESSAGE = "KNOWN_ACCIDENTS";

	// Private vars
	private Map<DTNHost, Double> hostDelayMap;
	// Message sending interval between two connected nodes
	private double sendInterval = 500;
	// Message size
	private int messageSize = 10;

	private static int uid = 0;

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
			this.messageSize = s.getInt(MESSAGE_SIZE);
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
		/* direct drop the message if host is at evacuation center */
		if (host.getDangerMode() == DangerMovement.EVAC_MODE) {
			return null;
		}

		/* Warning flag handling */
		if (msg.getProperty(DANGER_KEY_MESSAGE) != null) {
			host.setWarned(true);
		}

		/* Updating known locations map */
		if (msg.getProperty(KNOWN_LOCATIONS_KEY_MESSAGE) != null) {
			// System.err.println("[debug] node " + host.getAddress()
			// + " receive 1 message with his known locations");
			@SuppressWarnings("unchecked")
			Map<DTNHost, Tuple<Coord, Integer>> knownLocationsMsg = (Map<DTNHost, Tuple<Coord, Integer>>) msg
					.getProperty(KNOWN_LOCATIONS_KEY_MESSAGE);
			// add map B element or update if already in map res
			for (DTNHost hostMsg : knownLocationsMsg.keySet()) {
				Coord c = knownLocationsMsg.get(hostMsg).getKey();
				int stamp = knownLocationsMsg.get(hostMsg).getValue();
				host.updateKnownLocations(hostMsg, c, stamp);
			}
		}

		/* Updating list of known accidents */
		if (msg.getProperty(KNOWN_ACCIDENTS_KEY_MESSAGE) != null) {
			@SuppressWarnings("unchecked")
			Set<MapNode> knownAccidentsMsg = (Set<MapNode>) msg
					.getProperty(KNOWN_ACCIDENTS_KEY_MESSAGE);
			for (MapNode node : knownAccidentsMsg) {
				host.addAccidentAt(node);
			}
		}

		return null;
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

		/* do nothing if host is at evacuation center */
		if (host.getDangerMode() == DangerMovement.EVAC_MODE) {
			return;
		}

		List<Connection> connections = host.getConnections();

		if (connections.size() == 0) {
			hostDelayMap.clear();
			return;
		}

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
				// System.err.println("Message from " + host.getAddress() +
				// " to "
				// + h.getAddress());
				hostDelayMap.put(h, SimClock.getTime());
				Message m = new Message(host, h, "danger"
						+ SimClock.getIntTime() + "-" + host.getAddress() + "-"
						+ uid++, messageSize);

				/* Add warning flag if host is warned */
				if (host.isWarned()) {
					m.addProperty(DANGER_KEY_MESSAGE, true);
				}

				/* Add known locations in the message */
				host.updateSelfKnownLocation();
				m.addProperty(KNOWN_LOCATIONS_KEY_MESSAGE,
						host.getKnownLocations());

				/* Add known accidents in the message */
				m.addProperty(KNOWN_ACCIDENTS_KEY_MESSAGE,
						host.getKnownAccidents());

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

	public static int getNrofMessages() {
		return uid;
	}

}