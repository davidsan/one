package report;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import applications.DangerApplication;
import core.DTNHost;
import core.SimScenario;
import core.UpdateListener;
import db.Queries;

public class TimeReport extends ReportDB implements UpdateListener {

	protected PreparedStatement statement;

	public TimeReport() {
		super();
	}

	@Override
	public void updated(List<DTNHost> hosts) {

	}

	@Override
	void initTable() {
		try {
			Statement s = connection.createStatement();
			s.setQueryTimeout(30); // set timeout to 30 sec.
			s.executeUpdate(Queries.getQuery("TimeReportDB.drop_table"));
			s.executeUpdate(Queries.getQuery("TimeReportDB.create_table"));
			s.close();
			statement = connection.prepareStatement(Queries
					.getQuery("TimeReportDB.insert_into"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void done() {
		double timeEnd = SimScenario.getInstance().getEndTime();
		double canTimeEnd = SimScenario.getInstance().getCanEndTime();
		int nbOfDTNHosts = SimScenario.getInstance().getHosts().size();
		int nbOfMessages = DangerApplication.getNbOfMessages();
		try {
			statement.setDouble(1, timeEnd);
			statement.setDouble(2, canTimeEnd);
			statement.setInt(3, nbOfDTNHosts);
			statement.setInt(4, nbOfMessages);
			statement.setDouble(5, (1.0 * nbOfMessages) / nbOfDTNHosts);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		super.done();
	}

}
