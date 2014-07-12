package movement;

import java.util.List;

import movement.map.DijkstraPathFinder;
import movement.map.DijkstraPathFinderOptimal;
import movement.map.MapNode;
import movement.map.PointsOfInterestEvac;
import core.DTNHost;
import core.Settings;

/**
 * Map based movement model that uses Dijkstra's algorithm to find shortest
 * paths between two random map nodes and Points Of Interest
 * 
 * @author Virginie Collombon, David San
 */
public class ShortestPathMapBasedPoiMovement extends MapBasedMovement implements
		SwitchableMovement {
	public static final String PROBABILITY_TO_CHOOSE_RANDOM_POI = "randomPoi";
	/** the Dijkstra shortest path finder */
	private DijkstraPathFinder pathFinder;
	private DijkstraPathFinderOptimal pathFinderPoi;

	/** Points Of Interest handler */
	private PointsOfInterestEvac pois;

	private boolean ready;

	private MapNode to;
	private double randomPoiProb;
	private boolean chooseRandomPoi;
	private List<MapNode> nodePath;

	/**
	 * Creates a new movement model based on a Settings object's settings.
	 * 
	 * @param settings
	 *            The Settings object where the settings are read from
	 */
	public ShortestPathMapBasedPoiMovement(Settings settings) {
		super(settings);
		this.pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
		this.pathFinderPoi = new DijkstraPathFinderOptimal(getOkMapNodeTypes());
		this.pois = new PointsOfInterestEvac(getMap(), getOkMapNodeTypes(),
				settings, rng);
		this.ready = true;
		randomPoiProb = settings.getDouble(PROBABILITY_TO_CHOOSE_RANDOM_POI);
		chooseRandomPoi = rng.nextDouble() < randomPoiProb;
		to = null;
		nodePath = null;
	}

	/**
	 * Copyconstructor.
	 * 
	 * @param mbm
	 *            The ShortestPathMapBasedPoiMovement prototype to base the new
	 *            object to
	 */
	protected ShortestPathMapBasedPoiMovement(
			ShortestPathMapBasedPoiMovement mbm) {
		super(mbm);
		this.pathFinder = mbm.pathFinder;
		this.pathFinderPoi = mbm.pathFinderPoi;
		this.pois = mbm.pois;
		this.ready = mbm.ready;
		this.randomPoiProb = mbm.randomPoiProb;
		chooseRandomPoi = rng.nextDouble() < randomPoiProb;
		to = mbm.to;
		nodePath = mbm.nodePath;
	}

	@Override
	public Path getPath() {
		getHost().setDangerMode(DangerMovement.SHORT_MODE);
		Path p = new Path(generateSpeed());

		// discover accidents among the neighbors of the current node
		for (MapNode neighbor : lastMapNode.getNeighbors()) {
			if (neighbor.isClosed()) {
				// discovery
				host.addAccidentAt(neighbor);
			}
		}

		if (chooseRandomPoi && to == null) {
			to = pois.selectDestinationRandom();
		}

		// if the path was not computed
		if (nodePath == null || nodePath.isEmpty()
				|| getHost().isRecalculatingRoute()) {
			if (!chooseRandomPoi) {
				to = pois.selectDestination(lastMapNode, pathFinderPoi);
			}
			nodePath = pathFinder.getShortestPath(lastMapNode, to);
			host.setNodePath(nodePath); // for danger app
			getHost().setRecalculatingRoute(false);
		} else {
			// existing node path, we pop the head
			nodePath.remove(0);
		}

		// this assertion should never fire if the map is checked in read
		// phase
		assert nodePath.size() > 0 : "No path from " + lastMapNode + " to "
				+ to + ". The simulation map isn't fully connected";

		if (nodePath.size() < 1) {
			if (getHost() != null) {
				getHost().setStucked(true);
			}
			return p;
		}

		// host walk toward the road if he was off-road
		p.addWaypoint(nodePath.get(0).getLocation());
		if (nodePath.size() < 2) {
			lastMapNode = nodePath.get(0);
		} else {
			p.addWaypoint(nodePath.get(1).getLocation());
			lastMapNode = nodePath.get(1);
		}
		return p;

	}

	@Override
	public ShortestPathMapBasedPoiMovement replicate() {
		return new ShortestPathMapBasedPoiMovement(this);
	}

	public PointsOfInterestEvac getPois() {
		return pois;
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	@Override
	public void setHost(DTNHost host) {
		super.setHost(host);
		pathFinder.setHost(host);
	}

}
