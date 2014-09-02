package report;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map.Entry;

import util.Tuple;
import core.Coord;
import core.DTNHost;
import core.Settings;
import core.UpdateListener;
import db.Database;
import db.Queries;

/**
 * Location report stored in database
 * 
 * @author Virginie Collombon, David San
 */
public class LocationReportDB extends ReportDB implements UpdateListener {

	protected PreparedStatement statement;
	private int batchCount;

	public static final String UPDATE_RATE_S = "updateRate";
	private int updateRate;
	private int stepCount;

	/**
	 * Constructor.
	 */
	public LocationReportDB() {
		super();
		Settings settings = getSettings();
		updateRate = settings.getInt(UPDATE_RATE_S);
	}

	protected void initTable() {
		batchCount = 0;
		stepCount = updateRate;
		try {
			Statement s = connection.createStatement();
			s.setQueryTimeout(30); // set timeout to 30 sec.
			s.executeUpdate(Queries.getQuery("LocationReportDB.drop_table"));
			s.executeUpdate(Queries.getQuery("LocationReportDB.create_table"));
			s.close();
			statement = connection.prepareStatement(Queries
					.getQuery("LocationReportDB.insert_into"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updated(List<DTNHost> hosts) {
		if (stepCount <= 0) {
			int time = (int) getSimTime();
			for (DTNHost host : hosts) {
				try {
					/* manually update his own location */
					host.updateSelfKnownLocation();

					for (Entry<DTNHost, Tuple<Coord, Integer>> entry : host
							.getKnownLocations().entrySet()) {
						DTNHost knownHost = entry.getKey();
						Tuple<Coord, Integer> tuple = entry.getValue();
						Coord knownHostLoc = tuple.getKey();
						int stamp = tuple.getValue();

						int address = host.getAddress();
						int knownHostAddress = knownHost.getAddress();
						double knownHostLocationX = knownHostLoc.getX();
						double knownHostLocationY = knownHostLoc.getY();

						statement.setInt(1, time);
						statement.setInt(2, address);
						statement.setInt(3, knownHostAddress);
						statement.setDouble(4, knownHostLocationX);
						statement.setDouble(5, knownHostLocationY);
						statement.setInt(6, stamp);
						statement.addBatch();
						batchCount++;
						if (batchCount++ >= Database.BATCH_SAFE_LIMIT) {
							statement.executeBatch();
							batchCount = 0;
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			stepCount = updateRate;
		}
		stepCount--;
	}

	@Override
	public void done() {
		try {
			statement.executeBatch();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		super.done();
	}

}
