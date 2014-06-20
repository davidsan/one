package movement;

import movement.map.MapNode;
import routing.DangerRouter;
import core.Coord;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.SimScenario;

/**
 * 
 * This movement model makes use of several other movement models to simulate
 * movement in crisis situation.
 * 
 * @author Virginie Collombon, David San
 */
public class DangerMovement extends ExtendedMovementModel {

	public static final String PROBABILITY_TO_WALK = "walkProb";
	public static final String PROBABILITY_TO_BE_PREWARNED = "prewarnedProb";
	public static final String PROBABILITY_TO_BE_SELFWARNED = "selfwarnedProb";
	public static final String TIME_TO_WALK = "walkTime";
	public static final String MAX_SELFWARNED = "maxselfwarnedProb";

	private HomeMovement homeMM;
	private RandomPathMapBasedMovement walkMM;
	private ShortestPathMapBasedPoiMovement shortMM;
	private EvacuationCenterMovement evacMM;

	public static final int HOME_MODE = 0;
	public static final int WALK_MODE = 1;
	public static final int SHORT_MODE = 2;
	public static final int EVAC_MODE = 3;

	private int mode;
	private static int nrofHostsWarned = 0;
	private static int nrofHosts = 0;

	private double selfwarnedProb;
	private double walkProb;
	private double walkTime;
	private double prewarnedProb;
	private double maxselfwarnedProb;
	private static boolean onePrintPlease = true;

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
		walkMM = new RandomPathMapBasedMovement(settings);

		walkProb = settings.getDouble(PROBABILITY_TO_WALK);
		selfwarnedProb = settings.getDouble(PROBABILITY_TO_BE_SELFWARNED);
		walkTime = settings.getDouble(TIME_TO_WALK);
		prewarnedProb = settings.getDouble(PROBABILITY_TO_BE_PREWARNED);
		maxselfwarnedProb = settings.getDouble(MAX_SELFWARNED);

		if (rng.nextDouble() < prewarnedProb) {
			mode = SHORT_MODE;
			setCurrentMovementModel(shortMM);
		} else {
			if (rng.nextDouble() > walkProb) {
				mode = HOME_MODE;
				setCurrentMovementModel(homeMM);
			} else {
				mode = WALK_MODE;
				setCurrentMovementModel(walkMM);
			}
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
		walkMM = new RandomPathMapBasedMovement(proto.walkMM);

		maxselfwarnedProb = proto.maxselfwarnedProb;
		walkProb = proto.walkProb;
		selfwarnedProb = proto.selfwarnedProb;
		walkTime = proto.walkTime;
		prewarnedProb = proto.prewarnedProb;

		nrofHosts++;

		if (rng.nextDouble() < prewarnedProb) {
			mode = SHORT_MODE;
			setCurrentMovementModel(shortMM);
		} else {
			if (rng.nextDouble() > walkProb) {
				mode = HOME_MODE;
				setCurrentMovementModel(homeMM);
			} else {
				mode = WALK_MODE;
				setCurrentMovementModel(walkMM);
			}
		}
	}

	@Override
	public boolean newOrders() {
		switch (mode) {
		case HOME_MODE:
			// check for danger message
			for (Message m : this.host.getMessageCollection()) {
				if (m.getProperty(DangerRouter.KEY_MESSAGE) != null) {
					mode = SHORT_MODE;
					setCurrentMovementModel(shortMM);
					break;
				}
			}
			// selfwarn
			double nrofHostToWarn = maxselfwarnedProb * nrofHosts;
			if (nrofHostsWarned < nrofHostToWarn) {
				if (rng.nextDouble() < selfwarnedProb) {
					mode = SHORT_MODE;
					setCurrentMovementModel(shortMM);
				}
			} else {
				if (onePrintPlease) {
					System.out.println("Simulation can end now @"
							+ SimClock.getIntTime() + " / "
							+ SimScenario.getInstance().getEndTime());
					onePrintPlease = false;
				}
			}
			break;
		case SHORT_MODE:
			this.host.setWarned(true);
			if (shortMM.isReady()) {
				Coord coordLastMapNode = shortMM.lastMapNode.getLocation();
				// check if the node is at a evac center
				for (MapNode mn : shortMM.getPois().getPoiLists()) {
					Coord c = mn.getLocation();
					if (c.equals(coordLastMapNode)) {
						// the node is at the evacuation center
						mode = EVAC_MODE;
						setCurrentMovementModel(evacMM);
						break;
					}
				}
			}
			break;
		case EVAC_MODE:
			nrofHostsWarned++;
			break;
		case WALK_MODE:
			walkMM.setLocation(getHost().getLocation()); // update his home
			mode = WALK_MODE;
			setCurrentMovementModel(walkMM);
			double walkTimeCurrent = SimClock.getTime();
			if (walkTimeCurrent > walkTime) { // check if time is up
				mode = HOME_MODE;
				setCurrentMovementModel(homeMM);
			}

			for (Message m : this.host.getMessageCollection()) {
				if (m.getProperty(DangerRouter.KEY_MESSAGE) != null) {
					shortMM.setLocation(host.getLocation());
					mode = SHORT_MODE;
					setCurrentMovementModel(shortMM);
					break;
				}
			}
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public Coord getInitialLocation() {
		Coord homeLoc = shortMM.getInitialLocation().clone();
		walkMM.setLocation(homeLoc);
		shortMM.setLocation(homeLoc);
		return homeLoc;
	}

	@Override
	public MovementModel replicate() {
		return new DangerMovement(this);
	}
	
	@Override
	public void setHost(DTNHost host) {
		super.setHost(host);
		homeMM.setHost(host);
		walkMM.setHost(host);
		evacMM.setHost(host);
		shortMM.setHost(host);
	}
}
