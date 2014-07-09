package movement;

import java.util.List;

import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.PointsOfInterest;
import core.DTNHost;
import core.Settings;

/**
 * 
 * A Class to model random walking movement for Danger Movement Model
 * 
 * @author Virginie Collombon, David San
 */
public class RandomPathMapBasedMovement extends MapBasedMovement implements
		SwitchableMovement {
	/** the Dijkstra shortest path finder */
	private DijkstraPathFinder pathFinder;

	/** Points Of Interest handler */
	private PointsOfInterest pois;

	private MapNode to;
	private List<MapNode> nodePath;

	/**
	 * Creates a new movement model based on a Settings object's settings.
	 * 
	 * @param settings
	 *            The Settings object where the settings are read from
	 */
	public RandomPathMapBasedMovement(Settings settings) {
		super(settings);
		this.pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
		this.pois = new PointsOfInterest(getMap(), getOkMapNodeTypes(),
				settings, rng);
		this.to = null;
		this.nodePath = null;
	}

	/**
	 * Copyconstructor.
	 * 
	 * @param mbm
	 *            The ShortestPathMapBasedMovement prototype to base the new
	 *            object to
	 */
	protected RandomPathMapBasedMovement(RandomPathMapBasedMovement mbm) {
		super(mbm);
		this.pathFinder = mbm.pathFinder;
		this.pois = mbm.pois;
		this.to = mbm.to;
		this.nodePath = mbm.nodePath;
	}

	@Override
	public Path getPath() {
		getHost().setDangerMode(DangerMovement.WALK_MODE);
		Path p = new Path(generateSpeed());

		boolean discovery = false;
		// discover accidents among the neighbors of the current node
		for (MapNode neighbor : lastMapNode.getNeighbors()) {
			if (neighbor.isClosed()) {
				// discovery
				host.addAccidentAt(neighbor);
				discovery = true;
			}
		}

		if (to == null || nodePath.isEmpty()) {
			to = pois.selectDestination();
		}

		if (nodePath == null || nodePath.isEmpty() || discovery) {
			nodePath = pathFinder.getShortestPath(lastMapNode, to);
		} else {
			nodePath.remove(0);
		}
		// this assertion should never fire if the map is checked in read phase
		assert nodePath.size() > 0 : "No path from " + lastMapNode + " to "
				+ to + ". The simulation map isn't fully connected";

		if (nodePath.size() < 1) {
			return p;
		}

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
	public RandomPathMapBasedMovement replicate() {
		return new RandomPathMapBasedMovement(this);
	}

	@Override
	public void setHost(DTNHost host) {
		super.setHost(host);
		pathFinder.setHost(host);
	}
}
