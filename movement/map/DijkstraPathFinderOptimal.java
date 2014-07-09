/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package movement.map;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import core.DTNHost;

/**
 * Implementation of the Dijkstra's shortest path algorithm.
 * Compute Dijkstra if necessary.
 */
public class DijkstraPathFinderOptimal {
	/** Value for infinite distance */
	private static final Double INFINITY = Double.MAX_VALUE;
	/** Initial size of the priority queue */
	private static final int PQ_INIT_SIZE = 11;

	/** Map of node distances from the source node */
	private DistanceMap distances;
	/** Set of already visited nodes (where the shortest path is known) */
	private Set<MapNode> visited;
	/** Priority queue of unvisited nodes discovered so far */
	private Queue<MapNode> unvisited;
	/** Map of previous nodes on the shortest path(s) */
	private Map<MapNode, MapNode> prevNodes;

	private MapNode latestFrom;

	private int[] okMapNodes;
	private DTNHost host;

	/**
	 * Constructor.
	 * 
	 * @param okMapNodes
	 *            The map node types that are OK for paths or null if all nodes
	 *            are OK
	 */
	public DijkstraPathFinderOptimal(int[] okMapNodes) {
		super();
		this.okMapNodes = okMapNodes;
		this.latestFrom = null;
	}

	/**
	 * Initializes a new search with a source node
	 * 
	 * @param node
	 *            The path's source node
	 */
	private void initWith(MapNode node) {
		assert (okMapNodes != null ? node.isType(okMapNodes) : true);

		// create needed data structures
		this.unvisited = new PriorityQueue<MapNode>(PQ_INIT_SIZE,
				new DistanceComparator());
		this.visited = new HashSet<MapNode>();
		this.prevNodes = new HashMap<MapNode, MapNode>();
		this.distances = new DistanceMap();

		// set distance to source 0 and initialize unvisited queue
		this.distances.put(node, 0);
		this.unvisited.add(node);

		// discover accidents among the neighbors of the current node
		for (MapNode neighbor : node.getNeighbors()) {
			if (neighbor.isClosed()) {
				// discovery
				if (host != null) {
					host.addAccidentAt(neighbor);
				}
			}
		}
	}

	/**
	 * Finds and returns a shortest path between two map nodes
	 * 
	 * @param from
	 *            The source of the path
	 * @param to
	 *            The destination of the path
	 * @return a shortest path between the source and destination nodes in a
	 *         list of MapNodes or an empty list if such path is not available
	 */
	public List<MapNode> getShortestPath(MapNode from, MapNode to) {
		List<MapNode> path = new LinkedList<MapNode>();

		// compute dijkstra if necessary
		computeDijkstra(from);

		// now we either have the path or such path wasn't available
		if (visited.contains(to)) { // found a path
			path.add(0, to);
			MapNode prev = prevNodes.get(to);
			while (prev != from) {
				path.add(0, prev); // always put previous node to beginning
				prev = prevNodes.get(prev);
			}
			path.add(0, from); // finally put the source node to first node
		}
		return path;
	}

	/**
	 * Compute Dijkstra if it was not computed for the map node
	 * 
	 * @param from
	 *            Map node from
	 */
	private void computeDijkstra(MapNode from) {

		if (!(latestFrom != null && (from.compareTo(latestFrom) == 0))) {
			initWith(from);
			MapNode node = null;

			// always take the node with shortest distance
			while ((node = unvisited.poll()) != null) {
				visited.add(node); // mark the node as visited
				relax(node); // add/update neighbor nodes' distances
			}
		}
		latestFrom = from;

	}

	/**
	 * Return the distance between two map nodes, compute Dijkstra if necessary
	 * 
	 * @param from
	 *            The source of the path
	 * @param to
	 *            The destination of the path
	 * @return the minimal distance between the source and destination
	 */
	public Double getShortestDistance(MapNode from, MapNode to) {

		// compute dijkstra if necessary
		computeDijkstra(from);

		return distances.get(to);
	}

	/**
	 * Relaxes the neighbors of a node (updates the shortest distances).
	 * 
	 * @param node
	 *            The node whose neighbors are relaxed
	 */
	private void relax(MapNode node) {
		double nodeDist = distances.get(node);
		for (MapNode n : node.getNeighbors()) {
			if (visited.contains(n)) {
				continue; // skip visited nodes
			}

			if (okMapNodes != null && !n.isType(okMapNodes)) {
				continue; // skip nodes that are not OK
			}

			// n node's distance from path's source node
			double nDist = nodeDist + getDistance(node, n);

			if (distances.get(n) > nDist) { // stored distance > found dist?
				prevNodes.put(n, node);
				setDistance(n, nDist);
			}
		}
	}

	/**
	 * Sets the distance from source node to a node
	 * 
	 * @param n
	 *            The node whose distance is set
	 * @param distance
	 *            The distance of the node from the source node
	 */
	private void setDistance(MapNode n, double distance) {
		unvisited.remove(n); // remove node from old place in the queue
		distances.put(n, distance); // update distance
		unvisited.add(n); // insert node to the new place in the queue
	}

	/**
	 * Returns the (euclidean) distance between the two map nodes
	 * 
	 * @param from
	 *            The first node
	 * @param to
	 *            The second node
	 * @return Euclidean distance between the two map nodes
	 */
	private double getDistance(MapNode from, MapNode to) {
		if (host != null) {
			// return infinity for node aware of the accident
			if (from.isClosed() && host.isAwareOfAccidentAt(from)) {
				return INFINITY;
			}
		}
		return from.getLocation().distance(to.getLocation());
	}

	/**
	 * Comparator that compares two map nodes by their distance from the source
	 * node.
	 */
	private class DistanceComparator implements Comparator<MapNode> {

		/**
		 * Compares two map nodes by their distance from the source node
		 * 
		 * @return -1, 0 or 1 if node1's distance is smaller, equal to, or
		 *         bigger than node2's distance
		 */
		public int compare(MapNode node1, MapNode node2) {
			double dist1 = distances.get(node1);
			double dist2 = distances.get(node2);

			if (dist1 > dist2) {
				return 1;
			} else if (dist1 < dist2) {
				return -1;
			} else {
				return node1.compareTo(node2);
			}
		}
	}

	/**
	 * Simple Map implementation for storing distances.
	 */
	private class DistanceMap {
		private HashMap<MapNode, Double> map;

		/**
		 * Constructor. Creates an empty distance map
		 */
		public DistanceMap() {
			this.map = new HashMap<MapNode, Double>();
		}

		/**
		 * Returns the distance to a node. If no distance value is found,
		 * returns {@link DijkstraPathFinder#INFINITY} as the value.
		 * 
		 * @param node
		 *            The node whose distance is requested
		 * @return The distance to that node
		 */
		public double get(MapNode node) {
			Double value = map.get(node);
			if (value != null) {
				return value;
			} else {
				return INFINITY;
			}
		}

		/**
		 * Puts a new distance value for a map node
		 * 
		 * @param node
		 *            The node
		 * @param distance
		 *            Distance to that node
		 */
		public void put(MapNode node, double distance) {
			map.put(node, distance);
		}

		/**
		 * Returns a string representation of the map's contents
		 * 
		 * @return a string representation of the map's contents
		 */
		public String toString() {
			return map.toString();
		}
	}

	/**
	 * Sets the host
	 * 
	 * @param host
	 *            Host
	 */
	public void setHost(DTNHost host) {
		this.host = host;
	}
}