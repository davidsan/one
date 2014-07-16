package applications;

import java.util.Map;

import util.Tuple;
import core.Application;
import core.Coord;
import core.DTNHost;
import core.Message;
import core.Settings;

/**
 * Hotspot application for evacuation centers
 * 
 * @author Virginie Collombon
 * @author David San
 */

public class HotspotApplication extends Application {

	/** Application ID */
	// Hotspot app should handle message from Danger app
	public static final String APP_ID = "applications.DangerApplication";

	/**
	 * Creates a new hotspot application with the given settings.
	 * 
	 * @param s
	 *            Settings to use for initializing the application.
	 */
	public HotspotApplication(Settings s) {
		super.setAppID(APP_ID);
	}

	/**
	 * Copy-constructor
	 * 
	 * @param a
	 */
	public HotspotApplication(HotspotApplication a) {
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
		if (msg.getProperty(DangerApplication.KNOWN_LOCATIONS_KEY_MESSAGE) != null) {
			@SuppressWarnings("unchecked")
			Map<DTNHost, Tuple<Coord, Integer>> knownLocationsMsg = (Map<DTNHost, Tuple<Coord, Integer>>) msg
					.getProperty(DangerApplication.KNOWN_LOCATIONS_KEY_MESSAGE);
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
		return new HotspotApplication(this);
	}

	/**
	 * Does nothing
	 * 
	 * @param host
	 *            to which the application instance is attached
	 */
	@Override
	public void update(DTNHost host) {

	}

}