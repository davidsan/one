package report;

import java.util.List;

import core.DTNHost;
import core.UpdateListener;

public class TestReport extends ReportSQL implements UpdateListener {

	@Override
	protected void init() {
		super.init();
		write("BEGIN TRANSACTION;");
		write("CREATE TABLE test(time integer, host integer,x float, y float);");
	}

	@Override
	public void updated(List<DTNHost> hosts) {
		double temps = getSimTime();
		for (DTNHost host : hosts) {
			write("INSERT INTO  \"test\" VALUES(" + temps + ","
					+ host.getAddress() + "," + host.getLocation().getX() + ","
					+ host.getLocation().getY() + ");");
		}
	}

	@Override
	public void done() {
		write("COMMIT;");
		super.done();
	}

}
