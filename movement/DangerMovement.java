package movement;

import core.Coord;
import core.Settings;

/**
 * 
 * This movement model makes use of several other movement models to simulate
 * movement in crisis situation.
 * 
 * @author Virginie Collombon, David San
 */
public class DangerMovement extends ExtendedMovementModel {

	private ShortestPathMapBasedPoiMovement shortMM;
	private EvacuationCenterMovement evacMM;

	private static final int SHORTEST_PATH_MODE = 0;
	private static final int EVAC_MODE = 1;

	private int mode;

	/**
	 * Creates a new instance of DangerMovement
	 * 
	 * @param settings
	 */
	public DangerMovement(Settings settings) {
		super(settings);
		shortMM = new ShortestPathMapBasedPoiMovement(settings);
		evacMM = new EvacuationCenterMovement(settings);
		mode = SHORTEST_PATH_MODE;
		setCurrentMovementModel(shortMM);

	}

	/**
	 * Creates a new instance of DangerMovement from a prototype
	 * 
	 * @param proto
	 */
	public DangerMovement(DangerMovement proto) {
		super(proto);
		shortMM = new ShortestPathMapBasedPoiMovement(proto.shortMM);
		evacMM = new EvacuationCenterMovement(proto.evacMM);
		mode = proto.mode;
		setCurrentMovementModel(shortMM);
	}

	@Override
	public boolean newOrders() {
		switch (mode) {
		case SHORTEST_PATH_MODE:
			if (shortMM.isReady()) {
				Path p = shortMM.getPath();
				Coord coordLastMapNode = shortMM.lastMapNode.getLocation();
				Coord coordEvac = p.getCoords().get(p.getCoords().size()-1);

				if (coordLastMapNode.compareTo(coordEvac)==0) {
					// System.out.println("Switch vers EVAC_MODE");
					evacMM.getPath();
					mode = EVAC_MODE;
					setCurrentMovementModel(evacMM);
				}
			}
			break;
		case EVAC_MODE:
			// System.err.println("Dans EVAC_MODE");
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public Coord getInitialLocation() {
		Coord homeLoc = shortMM.getInitialLocation().clone();
		shortMM.setLocation(homeLoc);
		return homeLoc;
	}

	@Override
	public MovementModel replicate() {
		return new DangerMovement(this);
	}

}
