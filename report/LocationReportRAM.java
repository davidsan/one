package report;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import core.DTNHost;
import core.Settings;
import core.UpdateListener;
import db.Database;
import db.Queries;

/**
 * Location report stored in an on-memory database
 * 
 * @author Virginie Collombon, David San
 */
public class LocationReportRAM extends ReportRAM implements UpdateListener {

	protected PreparedStatement statement;
	private int batchCount;

	public static final String UPDATE_RATE_S = "updateRate";
	private int updateRate;
	private int stepCount;

	/**
	 * Constructor.
	 */
	public LocationReportRAM() {
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

					for (DTNHost knownHost : host.getKnownLocations().keySet()) {
						int address = host.getAddress();
						int knownHostAddress = knownHost.getAddress();
						double knownHostLocationX = knownHost.getLocation()
								.getX();
						double knownHostLocationY = knownHost.getLocation()
								.getY();
						int stamp = host.getKnownLocations().get(knownHost)
								.getValue();

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
			// Dump the database contents to a file
			statement.executeUpdate("backup to " + outFileName);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		super.done();
	}

}
