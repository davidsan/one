package movement.map;

import input.WKTReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import movement.MovementModel;
import core.Coord;
import core.Settings;
import core.SettingsError;

/**
 * Handler for points of interest data.
 * 
 * @author Virginie Collombon, David San
 */
public class PointsOfInterestEvac {
	/** Points Of Interest settings namespace ({@value} ) */
	public static final String POI_NS = "PointsOfInterestEvac";
	/** Points Of Interest file path ({@value} ) */
	public static final String POI_FILE_S = "poiFile";

	/** map whose points all POIs are */
	private SimMap map;
	/** map node types that are OK to visit */
	private int[] okMapNodeTypes;
	/** list of all this POI instance's POI lists */
	private List<MapNode> poiLists;

	protected static Random rng;

	static {
		reset();
	}

	/**
	 * Constructor.
	 * 
	 * @param parentMap
	 *            The map whose MapNodes' subset the POIs are
	 * @param okMapNodeTypes
	 *            Array of map node types that are OK to visit or null if all
	 *            nodes are OK
	 * @param settings
	 *            The Settings object where settings are read from
	 * @param rng
	 *            The random number generator to use
	 */
	public PointsOfInterestEvac(SimMap parentMap, int[] okMapNodeTypes,
			Settings settings, Random rng) {
		this.poiLists = new ArrayList<MapNode>();

		this.map = parentMap;
		this.okMapNodeTypes = okMapNodeTypes;

		readPois(settings);
	}

	/**
	 * Selects a random destination from POIs or all MapNodes. Selecting among
	 * POI groups is done by their probabilities. If sum of their probabilities
	 * is less than 1.0 and the drawn random probability is bigger than the sum,
	 * a random MapNode is selected from the SimMap.
	 * 
	 * 
	 * @param lastMapNode
	 * @param pathFinder
	 * 
	 * @return A destination among POIs or all MapNodes
	 */
	public MapNode selectDestination(MapNode lastMapNode,
			DijkstraPathFinderOptimal pathFinder) {
		HashMap<Double, MapNode> hm = new HashMap<Double, MapNode>();

		for (Iterator<MapNode> it = poiLists.iterator(); it.hasNext();) {
			MapNode poi = (MapNode) it.next();

			// compute the path's distance
			Double distance = pathFinder.getShortestDistance(lastMapNode, poi);

			hm.put(distance, poi);
		}
		// return the closest POI from the list
		Double min = Collections.min(hm.keySet());
		return hm.get(min);
	}

	public MapNode selectDestinationRandom() {
		return poiLists.get(rng.nextInt(poiLists.size()));
	}

	/**
	 * Reads POI selections from given Settings and stores them to
	 * <CODE>poiLists</CODE>.
	 * 
	 * @param s
	 *            The settings file where group specific settings are read
	 * @throws Settings
	 *             error if there was an error while reading the file or some of
	 *             the settings had invalid value(s).
	 */
	private void readPois(Settings s) {
		Coord offset = map.getOffset();
		poiLists = readPoisOf(offset);
		map.setPois(poiLists);
	}

	/**
	 * Reads POIs from a file <CODE>{@value POI_FILE_S}</CODE> defined in
	 * Settings' namespace {@value POI_NS}.
	 * 
	 * @param offset
	 *            Offset of map data
	 * @return A list of MapNodes read from the POI file
	 * @throws Settings
	 *             error if there was an error while reading the file or some
	 *             coordinate in POI-file didn't match any MapNode in the SimMap
	 */
	private List<MapNode> readPoisOf(Coord offset) {
		List<MapNode> nodes = new ArrayList<MapNode>();
		Settings fileSettings = new Settings(POI_NS);
		WKTReader reader = new WKTReader();

		File poiFile = null;
		List<Coord> coords = null;
		try {
			poiFile = new File(fileSettings.getSetting(POI_FILE_S));
			coords = reader.readPoints(poiFile);
		} catch (IOException ioe) {
			throw new SettingsError("Couldn't read POI-data from file '"
					+ poiFile + "' defined in setting "
					+ fileSettings.getFullPropertyName(POI_FILE_S)
					+ " (cause: " + ioe.getMessage() + ")");
		}

		if (coords.size() == 0) {
			throw new SettingsError("Read a POI group of size 0 from "
					+ poiFile);
		}

		for (Coord c : coords) {
			if (map.isMirrored()) { // mirror POIs if map data is also mirrored
				c.setLocation(c.getX(), -c.getY()); // flip around X axis
			}

			// translate to match map data
			c.translate(offset.getX(), offset.getY());

			MapNode node = map.getNodeByCoord(c);
			if (node != null) {
				if (okMapNodeTypes != null && !node.isType(okMapNodeTypes)) {
					throw new SettingsError("POI " + node + " from file "
							+ poiFile + " is on a part of the map that is not "
							+ "allowed for this movement model");
				}
				nodes.add(node);
			} else {
				throw new SettingsError("No MapNode in SimMap at location " + c
						+ " (after translation) from file " + poiFile);
			}
		}

		return nodes;
	}

	public List<MapNode> getPoiLists() {
		return poiLists;
	}

	/**
	 * Resets all static fields to default values
	 */
	public static void reset() {
		Settings s = new Settings(MovementModel.MOVEMENT_MODEL_NS);
		if (s.contains(MovementModel.RNG_SEED)) {
			int seed = s.getInt(MovementModel.RNG_SEED);
			rng = new Random(seed);
		} else {
			rng = new Random(0);
		}
	}
}
