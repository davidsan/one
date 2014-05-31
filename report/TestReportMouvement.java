package report;

import core.Coord;
import core.DTNHost;
import core.MovementListener;

public class TestReportMouvement extends Report implements MovementListener {

	@Override
	protected void init() {
		super.init();
		write("BEGIN TRANSACTION;");
		write("CREATE TABLE test(time integer, host integer,x float, y float);");
	}

	@Override
	public void newDestination(DTNHost host, Coord destination, double speed) {
		double temps = getSimTime();
		write("INSERT INTO  \"test\" VALUES(" + temps + "," + host.getAddress()
				+ "," + host.getLocation().getX() + ","
				+ host.getLocation().getY() + ");");
	}

	@Override
	public void initialLocation(DTNHost host, Coord location) {
		double temps = getSimTime();
		write("INSERT INTO  \"test\" VALUES(" + temps + "," + host.getAddress()
				+ "," + host.getLocation().getX() + ","
				+ host.getLocation().getY() + ");");
	}

	@Override
	public void done() {
		write("COMMIT;");
		super.done();
	}

}
