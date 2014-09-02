package report;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map.Entry;

import util.Tuple;
import core.Coord;
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
				for (Entry<DTNHost, Tuple<Coord, Integer>> entry : host
						.getKnownLocations().entrySet()) {
					DTNHost knownHost = entry.getKey();
					Tuple<Coord, Integer> tuple = entry.getValue();
					Coord knownHostLoc = tuple.getKey();
					int stamp = tuple.getValue();

					int knownHostAddress = knownHost.getAddress();
					double knownHostLocationX = knownHostLoc.getX();
					double knownHostLocationY = knownHostLoc.getY();

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
