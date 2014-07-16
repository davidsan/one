package applications;

import java.util.Map;
import util.Tuple;
import core.Application;
import core.Coord;
import core.DTNHost;
import core.Message;
import core.Settings;

/**
 * Hotspot application react when receiving a position message.
 * 
 * @author Virginie Collombon
 * @author David San
 */

public class HotspotAppplication extends Application {
	/** Application ID */
	public static final String APP_ID = "applications.HotspotApplication";

	/** Known Location Key Message */
	public static final String KNOWN_LOCATIONS_KEY_MESSAGE = "KNOWN_LOCATIONS";

	/**
	 * Creates a new hotspot application with the given settings.
	 * 
	 * @param s
	 *            Settings to use for initializing the application.
	 */
	public HotspotAppplication(Settings s) {
		super.setAppID(APP_ID);
	}

	/**
	 * Copy-constructor
	 * 
	 * @param a
	 */
	public HotspotAppplication(HotspotAppplication a) {
		super(a);
	}

	/**
	 * Handles an incoming message.
	 * 
	 * @param msg
	 *            message received by the router
	 * @param host
	 *            host to which the application instance is attached
	 */
	@Override
	public Message handle(Message msg, DTNHost host) {
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
		return null;
	}

	@Override
	public Application replicate() {
		return new HotspotAppplication(this);
	}

	@Override
	public void update(DTNHost host) {
	}
}
