package report;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import core.Coord;
import core.DTNHost;
import core.MovementListener;
import db.Database;
import db.Queries;

/**
 * Movement report stored in database
 * 
 * @author Virginie Collombon, David San
 */
public class MovementReportDB extends ReportDB implements MovementListener {

	protected PreparedStatement statement;
	private int batchCount;

	/**
	 * Constructor.
	 */
	public MovementReportDB() {
		super();
	}

	protected void initTable() {
		batchCount = 0;
		try {
			Statement s = connection.createStatement();
			s.setQueryTimeout(30); // set timeout to 30 sec.
			s.executeUpdate(Queries
					.getQuery("MovementReportDB.drop_table_movement"));
			s.executeUpdate(Queries
					.getQuery("MovementReportDB.create_table_movement"));
			s.close();
			statement = connection.prepareStatement(Queries
					.getQuery("MovementReportDB.insert_into_movement"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void newDestination(DTNHost host, Coord destination, double speed) {
		int time = (int) getSimTime();
		try {
			statement.setInt(1, time);
			statement.setInt(2, host.getAddress());
			statement.setDouble(3, host.getLocation().getX());
			statement.setDouble(4, host.getLocation().getY());
			statement.setDouble(5, destination.getX());
			statement.setDouble(6, destination.getY());
			statement.addBatch();
			if (batchCount++ >= Database.BATCH_SAFE_LIMIT) {
				statement.executeBatch();
				batchCount = 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void initialLocation(DTNHost host, Coord location) {
		int time = (int) getSimTime();
		try {
			statement.setInt(1, time);
			statement.setInt(2, host.getAddress());
			statement.setDouble(3, host.getLocation().getX());
			statement.setDouble(4, host.getLocation().getY());
			statement.setDouble(5, host.getLocation().getX());
			statement.setDouble(6, host.getLocation().getY());
			statement.addBatch();
			if (batchCount++ >= Database.BATCH_SAFE_LIMIT) {
				statement.executeBatch();
				batchCount = 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
