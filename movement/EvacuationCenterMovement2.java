package movement;

import core.Coord;
import core.Settings;

public class EvacuationCenterMovement2 extends MapBasedMovement implements
		SwitchableMovement {

	/**
	 * Creates a new instance of EvacuationCenterMovement2
	 * 
	 * @param settings
	 */
	public EvacuationCenterMovement2(Settings settings) {
		super(settings);
	}

	/**
	 * Creates a new instance of EvacuationCenterMovement2 from a prototype
	 * 
	 * @param proto
	 */
	protected EvacuationCenterMovement2(EvacuationCenterMovement2 mbm) {
		super(mbm);
	}

	@Override
	public Path getPath() {
		Path p = new Path();
		Double x = getLastLocation().getX() - (0.5 - rng.nextDouble()) * 500.0;
		Double y = getLastLocation().getY() - (0.5 - rng.nextDouble()) * 500.0;
		if (x > getMaxX()) {
			x = (double) getMaxX();
		} else if (x < 0) {
			x = 0.0;
		}
		if (y > getMaxY()) {
			y = (double) getMaxY();
		} else if (y < 0) {
			y = 0.0;
		}
		Coord c = new Coord(x, y);
		double speed = 0.1;
		p.addWaypoint(c, speed);
		p.addWaypoint(getLastLocation(), speed);
		return p;
	}

	@Override
	public EvacuationCenterMovement2 replicate() {
		return new EvacuationCenterMovement2(this);
	}

	@Override
	protected double generateWaitTime() {
		return 0;
	}

}
