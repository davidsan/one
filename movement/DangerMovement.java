package movement;

import java.util.Collection;
import java.util.Iterator;

import movement.map.MapNode;
import core.Coord;
import core.Message;
import core.Settings;
import core.SimClock;

/**
 * 
 * This movement model makes use of several other movement models to simulate
 * movement in crisis situation.
 * 
 * @author Virginie Collombon, David San
 */
public class DangerMovement extends ExtendedMovementModel {

	public static final String PROBABILITY_TO_BE_PREWARNED = "prewarnedProb";
	public static final String PROBABILITY_TO_RAMDOM_WALK = "walkProb";
	public static final String PROBALITY_TO_SELF_WARNED ="selfwarnedProb";
	public static final String MESSAGE_ID_PREFIX_S = "prefix";
	public static final String TIME_TO_WALK="walkTime";

	private HomeMovement homeMM;
	private ShortestPathMapBasedPoiMovement shortMM;
	private EvacuationCenterMovement evacMM;
	private ShortestPathMapBasedMovement walkMM;

	private static final int HOME_MODE = 0;
	private static final int SHORT_MODE = 1;
	private static final int EVAC_MODE = 2;
	private static final int RANDOM_WALK_MODE = 3;

	private int mode;

	private double walkTime;
	private double walkProb;
	private double prewarnedProb;
	private double selfwarnedProb;
	private String prefix;

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
		walkMM = new ShortestPathMapBasedMovement(settings);
		prewarnedProb = settings.getDouble(PROBABILITY_TO_BE_PREWARNED);
		walkProb = settings.getDouble(PROBABILITY_TO_RAMDOM_WALK);
		selfwarnedProb = settings.getDouble(PROBABILITY_TO_BE_PREWARNED);
		walkTime = settings.getDouble(TIME_TO_WALK);
		prefix = settings.getSetting(MESSAGE_ID_PREFIX_S);
		if (rng.nextDouble() > prewarnedProb) {
			if (rng.nextDouble() > walkProb){
				mode = HOME_MODE;
				setCurrentMovementModel(homeMM);
			}else{
				mode = RANDOM_WALK_MODE;
				setCurrentMovementModel(walkMM);
			}
		}else {
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
		walkMM = new ShortestPathMapBasedMovement(proto.walkMM);
		prewarnedProb = proto.prewarnedProb;
		walkProb = proto.walkProb;
		selfwarnedProb = proto.selfwarnedProb;
		walkTime = proto.walkTime;
		prefix = proto.prefix;
		if (rng.nextDouble() > prewarnedProb) {
			if (rng.nextDouble() > walkProb){
				mode = HOME_MODE;
				setCurrentMovementModel(homeMM);
			}else{
				mode = RANDOM_WALK_MODE;
				setCurrentMovementModel(walkMM);
			}
		}else {
			mode = SHORT_MODE;
			setCurrentMovementModel(shortMM);
		}
	}

	@Override
	public boolean newOrders() {
		Collection<Message> messages = host.getMessageCollection();
		switch (mode) {
		case HOME_MODE:
			//Collection<Message> messages = host.getMessageCollection();
			for (Iterator<Message> iterator = messages.iterator(); iterator
					.hasNext();) {
				Message m = (Message) iterator.next();
				// check if it is a danger message
				if (m.getId().toLowerCase().contains(prefix.toLowerCase())
						&& !(m.getFrom().equals(host))) {
					mode = SHORT_MODE;
					setCurrentMovementModel(shortMM);
					break;
				}
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
		case RANDOM_WALK_MODE:
			System.out.println("je suis dans randomwalk");
			double walkTimeCurrent = SimClock.getTime();
			if(walkTimeCurrent > walkTime ){ // check if time is up
				System.out.println("timeup");
				mode = HOME_MODE;
				setCurrentMovementModel(homeMM);
				break; 
			}
			
			for (Iterator<Message> iterator = messages.iterator(); iterator
					.hasNext();) {
				Message m = (Message) iterator.next();
				if (m.getId().toLowerCase().contains(prefix.toLowerCase())
						&& !(m.getFrom().equals(host))) {
					System.out.println("msg");
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
         System.err.println(getHost().toString() + " : " + homeLoc);
         return homeLoc;
	}

	@Override
	public MovementModel replicate() {
		return new DangerMovement(this);
	}

}
