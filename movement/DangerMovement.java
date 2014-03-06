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

	public static final String PROBABILITY_TO_BE_PREWARNED = "prewarnedProb";

	private HomeMovement homeMM;
	private ShortestPathMapBasedPoiMovement shortMM;
	private EvacuationCenterMovement evacMM;

	private static final int HOME_MODE = 0;
	private static final int SHORT_MODE = 1;
	private static final int EVAC_MODE = 2;

	private int mode;

	private double prewarnedProb;

	/**
	 * Creates a new instance of DangerMovement
	 * 
	 * @param settings
	 */
	public DangerMovement(Settings settings) {
		super(settings);
		homeMM = new HomeMovement(settings);
		shortMM = new ShortestPathMapBasedPoiMovement(settings);
		evacMM = new EvacuationCenterMovement(settings);
		prewarnedProb = settings.getDouble(PROBABILITY_TO_BE_PREWARNED);
		if (rng.nextDouble()<prewarnedProb) {
			mode = HOME_MODE;
			setCurrentMovementModel(homeMM);
		} else {
			mode = SHORT_MODE;
			setCurrentMovementModel(shortMM);
		}

	}

	/**
	 * Creates a new instance of DangerMovement from a prototype
	 * 
	 * @param proto
	 */
	public DangerMovement(DangerMovement proto) {
		super(proto);
		homeMM = new HomeMovement(proto.homeMM);
		shortMM = new ShortestPathMapBasedPoiMovement(proto.shortMM);
		evacMM = new EvacuationCenterMovement(proto.evacMM);
		prewarnedProb = proto.prewarnedProb;
		if (rng.nextDouble()<prewarnedProb) {
			mode = HOME_MODE;
			setCurrentMovementModel(homeMM);
		} else {
			mode = SHORT_MODE;
			setCurrentMovementModel(shortMM);
		}
		// mode = proto.mode;
	}

	@Override
	public boolean newOrders() {
		switch (mode) {
		case HOME_MODE:
			System.err.println("Dans HOME_MODE");

			break;
		case SHORT_MODE:
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
