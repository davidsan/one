package report;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.List;

import core.DTNHost;
import core.Settings;
import core.UpdateListener;

/**
 * Location report stored in csv file
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
		try {
			out.write("time integer, host integer, known_host integer, known_location_x real, known_location_y real, freshness integer");
			out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updated(List<DTNHost> hosts) {
		if (stepCount <= 0) {
			int time = (int) getSimTime();
			for (DTNHost host : hosts) {
				/* manually update his own location */
				host.updateSelfKnownLocation();
				int address = host.getAddress();
				String prefix = time + "," + address + ",";
				for (DTNHost knownHost : host.getKnownLocations().keySet()) {
					int knownHostAddress = knownHost.getAddress();
					double knownHostLocationX = knownHost.getLocation().getX();
					double knownHostLocationY = knownHost.getLocation().getY();
					int stamp = host.getKnownLocations().get(knownHost)
							.getValue();
					try {
						out.write(prefix + knownHostAddress + ","
								+ knownHostLocationX + "," + knownHostLocationY
								+ "," + stamp);
						out.newLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
			stepCount = updateRate;
		}
		stepCount--;
	}

}
