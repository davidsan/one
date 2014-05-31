package report;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import core.DTNHost;
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

	/**
	 * Constructor.
	 */
	public LocationReportDB() {
		super();
	}

	protected void initTable() {
		batchCount = 0;
		try {
			Statement s = connection.createStatement();
			s.setQueryTimeout(30); // set timeout to 30 sec.
			s.executeUpdate(Queries
					.getQuery("LocationReportDB.drop_table_location"));
			s.executeUpdate(Queries
					.getQuery("LocationReportDB.create_table_location"));
			s.close();
			statement = connection.prepareStatement(Queries
					.getQuery("LocationReportDB.insert_into_location"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updated(List<DTNHost> hosts) {
		Double time = getSimTime();
		for (DTNHost host : hosts) {
			try {
				statement.setDouble(1, time);
				statement.setInt(2, host.getAddress());
				statement.setDouble(3, host.getLocation().getX());
				statement.setDouble(4, host.getLocation().getY());
				statement.addBatch();
				batchCount++;
				if (batchCount >= Database.BATCH_LIMIT) {
					batchCount = 0;
					statement.executeBatch();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

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
