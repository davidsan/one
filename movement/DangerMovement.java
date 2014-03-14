package movement;

import java.util.Collection;
import java.util.Iterator;

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

	public static final String PROBABILITY_TO_BE_PREWARNED = "prewarnedProb";
	public static final String MESSAGE_ID_PREFIX_S = "prefix";

	private HomeMovement homeMM;
	private ShortestPathMapBasedPoiMovement shortMM;
	private EvacuationCenterMovement evacMM;

	private static final int HOME_MODE = 0;
	private static final int SHORT_MODE = 1;
	private static final int EVAC_MODE = 2;

	private int mode;

	private double prewarnedProb;
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
		prewarnedProb = settings.getDouble(PROBABILITY_TO_BE_PREWARNED);
		prefix = settings.getSetting(MESSAGE_ID_PREFIX_S);
		if (rng.nextDouble() > prewarnedProb) {
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
		prefix = proto.prefix;
		if (rng.nextDouble() > prewarnedProb) {
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
			Collection<Message> messages = host.getMessageCollection();
			for (Iterator<Message> iterator = messages.iterator(); iterator
					.hasNext();) {
				Message m = (Message) iterator.next();
				// si message est de type danger
				if (m.getId().toLowerCase().contains(prefix.toLowerCase())
						&& !(m.getFrom().equals(host))) {
					// System.err.println(" HOME_MODE --> EVAC");
					mode = SHORT_MODE;
					setCurrentMovementModel(shortMM);
					break;
				}
			}
			break;
		case SHORT_MODE:
			if (shortMM.isReady()) {
				Path p = shortMM.getPath();
				Coord coordLastMapNode = shortMM.lastMapNode.getLocation();
				// parcours des centre d'évacuations pour voir si notre 
				// position correspond à l'un des centres
				for (MapNode mn : shortMM.getPois().getPoiLists()) {
					Coord c = mn.getLocation();
					if (c.equals(coordLastMapNode)) {
						// le noeud est au centre d'évacuation
						System.err.println("Node " + getHost() + " is safe and sound !");
						evacMM.getPath();
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
