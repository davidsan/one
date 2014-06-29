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
			Double time = getSimTime();
			for (DTNHost host : hosts) {
				try {
					statement.setDouble(1, time);
					statement.setInt(2, host.getAddress());
					statement.setDouble(3, host.getLocation().getX());
					statement.setDouble(4, host.getLocation().getY());
					statement.setInt(5, -1);
					statement.setDouble(6, -1);
					statement.setDouble(7, -1);
					statement.setInt(8, -1);
					statement.addBatch();
					System.out.println(time.intValue() + ";"
							+ host.getAddress() + ";"
							+ host.getLocation().getX() + ";"
							+ host.getLocation().getY() + ";" + -1 + ";" + -1
							+ ";" + -1 + ";" + -1);
					for (DTNHost knownHost : host.getKnownLocations().keySet()) {
						statement.setDouble(1, time);
						statement.setInt(2, host.getAddress());
						statement.setDouble(3, host.getLocation().getX());
						statement.setDouble(4, host.getLocation().getY());
						statement.setInt(5, knownHost.getAddress());
						statement.setDouble(6,
								host.getKnownLocations().get(knownHost)
										.getKey().getX());
						statement.setDouble(7,
								host.getKnownLocations().get(knownHost)
										.getKey().getY());
						statement.setInt(8,
								host.getKnownLocations().get(knownHost)
										.getValue());
						statement.addBatch();
						System.out.println(time.intValue()
								+ ";"
								+ host.getAddress()
								+ ";"
								+ host.getLocation().getX()
								+ ";"
								+ host.getLocation().getY()
								+ ";"
								+ knownHost.getAddress()
								+ ";"
								+ host.getKnownLocations().get(knownHost)
										.getKey().getX()
								+ ";"
								+ host.getKnownLocations().get(knownHost)
										.getKey().getY()
								+ ";"
								+ host.getKnownLocations().get(knownHost)
										.getValue());
						if (batchCount++ >= Database.BATCH_SAFE_LIMIT) {
							// System.err.println("executeBatch @" + time);
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
