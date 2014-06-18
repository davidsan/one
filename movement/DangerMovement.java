package movement;

import routing.DangerRouter;
import movement.map.MapNode;
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
	private SosMovement sosMM;

	public static final int HOME_MODE = 0;
	public static final int WALK_MODE = 1;
	public static final int SHORT_MODE = 2;
	public static final int EVAC_MODE = 3;
	public static final int SOS_MODE = 4;

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
		sosMM = new SosMovement(settings);

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
		setHostMode();
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
		sosMM = new SosMovement(proto.sosMM);

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
		setHostMode();
	}

	@Override
	public boolean newOrders() {
		double nrofHostToWarn = maxselfwarnedProb * nrofHosts;
		if (nrofHostsWarned >= nrofHostToWarn) {
			if (onePrintPlease) {
				System.out.println("Simulation can end now @"
						+ SimClock.getIntTime() + " / "
						+ SimScenario.getInstance().getEndTime());
				onePrintPlease = false;
			}
		}
		switch (mode) {
		case HOME_MODE:
			// sos mode
			if (nrofHostsWarned >= nrofHostToWarn) {
				mode = SOS_MODE;
				setCurrentMovementModel(sosMM);
				break;
			}
			// check for danger message
			for (Message m : this.host.getMessageCollection()) {
				if (m.getProperty(DangerRouter.KEY_MESSAGE) != null) {
					mode = SHORT_MODE;
					setCurrentMovementModel(shortMM);
					break;
				}
			}
			// selfwarn
			if (nrofHostsWarned < nrofHostToWarn) {
				if (rng.nextDouble() < selfwarnedProb) {
					mode = SHORT_MODE;
					setCurrentMovementModel(shortMM);
				}
			}
			break;
		case SHORT_MODE:
			this.host.setWarned(true);
			// sos mode
			if (nrofHostsWarned >= nrofHostToWarn) {
				if (host.isStucked()) {
					mode = SOS_MODE;
					setCurrentMovementModel(sosMM);
					break;
				}
			}
			if (shortMM.isReady()) {
				// Coord coordLastMapNode = shortMM.lastMapNode.getLocation();
				Coord coordLastMapNode = host.getLocation();
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
			// sos mode
			if (nrofHostsWarned >= nrofHostToWarn || host.isStucked()) {
				mode = SOS_MODE;
				setCurrentMovementModel(sosMM);
				break;
			}
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
		}
		setHostMode();
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

	private void setHostMode() {
		if (!(getHost() == null)) {
			getHost().setDangerMode(mode);
		}
	}

	@Override
	public void setHost(DTNHost host) {
		super.setHost(host);
		walkMM.setHost(host);
		shortMM.setHost(host);
	}

}
