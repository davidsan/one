package report;

import java.sql.PreparedStatement;
import java.util.List;

import core.DTNHost;
import core.Settings;
import core.UpdateListener;

/**
 * Location report stored in database
 * 
 * @author Virginie Collombon, David San
 */
public class LocationReportCSV extends ReportCSV implements UpdateListener {

	protected PreparedStatement statement;
	public static final String UPDATE_RATE_S = "updateRate";
	private int updateRate;
	private int stepCount;

	/**
	 * Constructor.
	 */
	public LocationReportCSV() {
		super();
		Settings settings = getSettings();
		updateRate = settings.getInt(UPDATE_RATE_S);
	}

	@Override
	void initCSVHeader() {
		out.println("time,host,known_host,known_location_x,known_location_y,freshness");
	}

	@Override
	public void updated(List<DTNHost> hosts) {
		if (stepCount <= 0) {
			Double time = getSimTime();
			for (DTNHost host : hosts) {
				/* manually update his own location */
				host.updateSelfKnownLocation();

				for (DTNHost knownHost : host.getKnownLocations().keySet()) {
					int address = host.getAddress();
					int knownHostAddress = knownHost.getAddress();
					double knownHostLocationX = knownHost.getLocation().getX();
					double knownHostLocationY = knownHost.getLocation().getY();
					int stamp = host.getKnownLocations().get(knownHost)
							.getValue();

					StringBuilder csvBuilder = new StringBuilder();
					csvBuilder.append(time + ",");
					csvBuilder.append(address + ",");
					csvBuilder.append(knownHostAddress + ",");
					csvBuilder.append(knownHostLocationX + ",");
					csvBuilder.append(knownHostLocationY + ",");
					csvBuilder.append(stamp);
					out.println(csvBuilder.toString());
				}

			}
			stepCount = updateRate;
		}
		stepCount--;
	}

}
