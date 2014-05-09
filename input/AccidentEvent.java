package input;

import java.util.Random;

import movement.map.MapNode;
import movement.map.SimMap;
import core.SimClock;
import core.SimScenario;
import core.World;
/**
 * A Class for accident event
 * 
 * @author Virginie Collombon, David San
 */
public class AccidentEvent extends ExternalEvent {

	private static final long serialVersionUID = 1L;

	public AccidentEvent(double time) {
		super(time);
	}

	@Override
	public void processEvent(World world) {
		SimMap map = SimScenario.getInstance().getMap();
		if (map == null) {
			return;
		}
		Random rng = new Random(SimClock.getIntTime());
		MapNode n = null;
		do {
			n = map.getNodes().get(rng.nextInt(map.getNodes().size()));
		} while (n.isClosed());
		n.close();
	}

	@Override
	public String toString() {
		return super.toString() + " [ ACCIDENT ] " + time + " ACCIDENT";
	}
}
