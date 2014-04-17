package movement;

import java.util.List;

import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.PointsOfInterestEvac;
import core.Settings;

/**
 * Map based movement model that uses Dijkstra's algorithm to find shortest
 * paths between two random map nodes and Points Of Interest
 * 
 * @author Virginie Collombon, David San
 */
public class ShortestPathMapBasedPoiMovement extends MapBasedMovement implements
		SwitchableMovement {
	/** the Dijkstra shortest path finder */
	private DijkstraPathFinder pathFinder;

	/** Points Of Interest handler */
	private PointsOfInterestEvac pois;

	private boolean ready;

	/**
	 * Creates a new movement model based on a Settings object's settings.
	 * 
	 * @param settings
	 *            The Settings object where the settings are read from
	 */
	public ShortestPathMapBasedPoiMovement(Settings settings) {
		super(settings);
		this.pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
		this.pois = new PointsOfInterestEvac(getMap(), getOkMapNodeTypes(),
				settings, rng);
		this.ready = true;
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
		this.pois = mbm.pois;
		this.ready = mbm.ready;
	}

	@Override
	public Path getPath() {
		Path p = new Path(generateSpeed());

		MapNode to = pois.selectDestination(lastMapNode, pathFinder);
		List<MapNode> nodePath = pathFinder.getShortestPath(lastMapNode, to);

		for (MapNode mapNode : nodePath) {
			if (mapNode.isClosed()) {
				System.err.println("hehe je sors");
				ready = false;
				return p;
			}
		}

		// this assertion should never fire if the map is checked in read
		// phase
		assert nodePath.size() > 0 : "No path from " + lastMapNode + " to "
				+ to + ". The simulation map isn't fully connected";

		if (nodePath.size() < 1) {
			ready = false;
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

}
