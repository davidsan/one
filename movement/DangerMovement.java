package movement;

import movement.map.MapNode;
import core.Coord;
import core.Message;
import core.Settings;

/**
 * 
 * This movement model makes use of several other movement models to simulate
 * movement in crisis situation.
 * 
 * @author Virginie Collombon, David San
 */
public class DangerMovement extends ExtendedMovementModel {

	public static final String MESSAGE_ID_PREFIX_S = "prefix";
	public static final String PROBABILITY_TO_BE_SELFWARNED = "selfwarnedProb";

	private HomeMovement homeMM;
	private ShortestPathMapBasedMovement randMM;
	private ShortestPathMapBasedPoiMovement shortMM;
	private EvacuationCenterMovement evacMM;

	private static final int HOME_MODE = 0;
	private static final int SHORT_MODE = 1;
	private static final int EVAC_MODE = 2;

	private int mode;

	private double prewarnedProb;
	private String prefix;
	private double selfwarnedProb;

	/**
	 * Creates a new instance of DangerMovement
	 * 
	 * @param settings
	 */
	public DangerMovement(Settings settings) {
		super(settings);
		homeMM = new HomeMovement(settings);
		randMM = new ShortestPathMapBasedMovement(settings);
		shortMM = new ShortestPathMapBasedPoiMovement(settings);
		evacMM = new EvacuationCenterMovement(settings);
		prefix = settings.getSetting(MESSAGE_ID_PREFIX_S);
		selfwarnedProb = settings.getDouble(PROBABILITY_TO_BE_SELFWARNED);

		mode = HOME_MODE;
		setCurrentMovementModel(homeMM);
	}

	/**
	 * Creates a new instance of DangerMovement from a prototype
	 * 
	 * @param proto
	 */
	public DangerMovement(DangerMovement proto) {
		super(proto);
		homeMM = new HomeMovement(proto.homeMM);
		randMM = new ShortestPathMapBasedMovement(proto.randMM);
		shortMM = new ShortestPathMapBasedPoiMovement(proto.shortMM);
		evacMM = new EvacuationCenterMovement(proto.evacMM);
		prewarnedProb = proto.prewarnedProb;
		prefix = proto.prefix;
		selfwarnedProb = proto.selfwarnedProb;

		mode = HOME_MODE;
		setCurrentMovementModel(homeMM);
	}

	@Override
	public boolean newOrders() {
		switch (mode) {
		case HOME_MODE:
			// check for danger message
			for (Message m : this.host.getMessageCollection()) {
				if (m.getId().toLowerCase().contains(prefix.toLowerCase())) {
					mode = SHORT_MODE;
					setCurrentMovementModel(shortMM);
					break;
				}
			}
			// selfwarn
			if (rng.nextDouble() < selfwarnedProb) {
				mode = SHORT_MODE;
				setCurrentMovementModel(shortMM);
				break;
			}
			break;
		case SHORT_MODE:
			if (shortMM.isReady()) {
				Coord coordLastMapNode = shortMM.lastMapNode.getLocation();
				// check if the node is at a evac center
				for (MapNode mn : shortMM.getPois().getPoiLists()) {
					Coord c = mn.getLocation();
					if (c.equals(coordLastMapNode)) {
						// the node is at the evacuation center
						mode = EVAC_MODE;
						setCurrentMovementModel(evacMM);
					}
				}
			}
			break;
		case EVAC_MODE:
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
