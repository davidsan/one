package movement;

import core.Settings;

/**
 * A Class to model movement at the evacuation center. Nodes stay at the
 * evacuation center.
 * 
 * @author Virginie Collombon, David San
 */
public class EvacuationCenterMovement extends MapBasedMovement implements
		SwitchableMovement {

	/**
	 * Creates a new instance of EvacuationCenterMovement
	 * 
	 * @param settings
	 */
	public EvacuationCenterMovement(Settings settings) {
		super(settings);
	}

	/**
	 * Creates a new instance of EvacuationCenterMovement from a prototype
	 * 
	 * @param proto
	 */
	protected EvacuationCenterMovement(EvacuationCenterMovement mbm) {
		super(mbm);
	}

	@Override
	public Path getPath() {
		getHost().setDangerMode(DangerMovement.EVAC_MODE);
		Path p = new Path();

		// // generate (x,y) within the circle of radius 250
		// Double a = 2 * Math.PI * Math.random();
		// Double r = Math.sqrt(Math.random());
		// Double x = getLastLocation().getX() + (r * 250.0) * Math.cos(a);
		// Double y = getLastLocation().getY() + (r * 250.0) * Math.sin(a);
		//
		// if (x > getMaxX()) {
		// x = (double) getMaxX();
		// } else if (x < 0) {
		// x = 0.0;
		// }
		// if (y > getMaxY()) {
		// y = (double) getMaxY();
		// } else if (y < 0) {
		// y = 0.0;
		// }
		// Coord c = new Coord(x, y);
		// double speed = 0.1;
		// p.addWaypoint(c, speed);
		// p.addWaypoint(getLastLocation(), speed);
		return p;
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public double nextPathAvailable() {
		return Double.MAX_VALUE; // no new paths available
	}

	@Override
	public EvacuationCenterMovement replicate() {
		return new EvacuationCenterMovement(this);
	}

}
