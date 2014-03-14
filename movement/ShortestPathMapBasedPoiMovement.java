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
	}

	@Override
	public Path getPath() {
		Path p = new Path(generateSpeed());

		MapNode to = pois.selectDestination(lastMapNode, pathFinder);
		List<MapNode> nodePath = pathFinder.getShortestPath(lastMapNode, to);

		// this assertion should never fire if the map is checked in read
		// phase
		assert nodePath.size() > 0 : "No path from " + lastMapNode + " to "
				+ to + ". The simulation map isn't fully connected";

		// BUGBUG il faut au moins 2 mapnode pr faire un tracé, sinon ça bouge
		// pas!
		if (nodePath.size() < 1) {
			return p;
		}
		p.addWaypoint(nodePath.get(0).getLocation());
		if (nodePath.size() < 2) {
			System.err.println("Le noeud " + getHost() + ".");
			lastMapNode = to; // BUGBUG si ça bouge pas, alors on est arrivé
			return p;
		}
		// BUGBUG : on ajoute le premier coord et le deuxieme coord au path
		p.addWaypoint(nodePath.get(1).getLocation());
		// BUGBUG : on dit que le lastmapnode c'est le deuxieme coord
		lastMapNode = nodePath.get(1);
		return p;
	}

	@Override
	public ShortestPathMapBasedPoiMovement replicate() {
		return new ShortestPathMapBasedPoiMovement(this);
	}

	public PointsOfInterestEvac getPois() {
		return pois;
	}

}
