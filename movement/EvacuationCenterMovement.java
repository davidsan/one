package movement;

import core.Settings;

public class EvacuationCenterMovement extends MapBasedMovement implements
        SwitchableMovement {

	public EvacuationCenterMovement(Settings settings) {
		super(settings);
	}

	protected EvacuationCenterMovement(EvacuationCenterMovement mbm) {
		super(mbm);
	}

	@Override
	public Path getPath() {
		Path p = new Path(0);
		p.addWaypoint(lastMapNode.getLocation());
		System.err.println("qsdqsdqs");
		return p;
	}

	@Override
	public EvacuationCenterMovement replicate() {
		return new EvacuationCenterMovement(this);
	}

}
