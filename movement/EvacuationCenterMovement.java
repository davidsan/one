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
		Path p = new Path(0);
		return p;
	}

	@Override
	public EvacuationCenterMovement replicate() {
		return new EvacuationCenterMovement(this);
	}

	@Override
	protected double generateWaitTime() {
		return Double.MAX_VALUE;
	}

}
