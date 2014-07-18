package movement;

import java.util.List;

import movement.map.MapNode;
import movement.map.PointsOfInterestEvac;
import core.Coord;
import core.Settings;

/**
 * Hotspot movement model
 * 
 * @author Virginie Collombon, David San
 */

public class HotspotMovement extends MapBasedMovement {

	private PointsOfInterestEvac pois;
	private static int indexPoi = 0;

	/**
	 * Creates a new movement model based on a Settings object's settings.
	 * 
	 * @param s
	 * The Settings object where the settings are read from
	 */
	public HotspotMovement(Settings s) {
		super(s);
		this.pois = new PointsOfInterestEvac(getMap(), getOkMapNodeTypes(), s,
				rng);
	}

	/**
	 * Copyconstructor.
	 * 
	 * @param h
	 * The HotspotMovement prototype to base the new object to
	 */
	public HotspotMovement(HotspotMovement h) {
		super(h);
		this.pois = h.pois;
	}

	@Override
	public Path getPath() {
		return new Path();
	}

	@Override
	public Coord getLastLocation() {
		System.out.println("yoyoyo222 y");
		return super.getLastLocation();
	}

	@Override
	public void setLocation(Coord lastWaypoint) {
		super.setLocation(lastWaypoint);
	}

	@Override
	public Coord getInitialLocation() {
		List<MapNode> poisList = pois.getPoiLists();
		return poisList.get(indexPoi++).getLocation();
	}

	@Override
	public HotspotMovement replicate() {
		return new HotspotMovement(this);
	}
	
}
