package tool;

import java.util.HashMap;
import java.util.Map;

public class TopologicalSortingPaths {

	private TopologicalSorting topoSort = new TopologicalSorting();

	// TODO Parameterize topological sort of RDG.
	private Map<RDGNode, Integer> numPathsVisit(RDGNode node, Map<RDGNode, Boolean> marks,
			Map<RDGNode, Map<RDGNode, Integer>> cache) throws CyclicRdgException {
		if (topoSort.isRunningTopologySort(node, marks)) {
			// Mark node temporarily (cycle detection)
			marks.put(node, false);

			Map<RDGNode, Integer> numberOfPaths = new HashMap<RDGNode, Integer>();
			// A node always has a path to itself.
			numberOfPaths.put(node, 1);
			// The number of paths from a node X to a node Y is equal to the
			// sum of the numbers of paths from each of its descendants to Y.
			for (RDGNode child : node.getDependencies()) {
				Map<RDGNode, Integer> tmpNumberOfPaths = numPathsVisit(child, marks, cache);
				numberOfPaths = sumPaths(numberOfPaths, tmpNumberOfPaths);
			}
			// Mark node permanently (finished sorting branch)
			marks.put(node, true);
			cache.put(node, numberOfPaths);
			return numberOfPaths;
		}
		// Otherwise, the node has already been visited.
		return cache.get(node);
	}

	/**
	 * Sums two paths-counting maps
	 * 
	 * @param pathsCountA
	 * @param pathsCountB
	 * @return
	 */
	private static Map<RDGNode, Integer> sumPaths(Map<RDGNode, Integer> pathsCountA,
			Map<RDGNode, Integer> pathsCountB) {
		Map<RDGNode, Integer> numberOfPaths = new HashMap<RDGNode, Integer>(pathsCountA);
		for (Map.Entry<RDGNode, Integer> entry : pathsCountB.entrySet()) {
			RDGNode node = entry.getKey();
			Integer count = entry.getValue();
			if (numberOfPaths.containsKey(node)) {
				count += numberOfPaths.get(node);
			}
			numberOfPaths.put(node, count);
		}
		return numberOfPaths;
	}

	/**
	 * Computes the number of paths from source nodes to every known node.
	 * 
	 * @return A map associating an RDGNode to the corresponding number of paths
	 *         from a source node which lead to it.
	 * @throws CyclicRdgException
	 */
	public Map<RDGNode, Integer> getNumberOfPaths(RDGNode node) throws CyclicRdgException {
		Map<RDGNode, Integer> numberOfPaths = new HashMap<RDGNode, Integer>();

		Map<RDGNode, Integer> tmpNumberOfPaths = getNumberMapsOfPathVisited(node);
		numberOfPaths = sumPaths(numberOfPaths, tmpNumberOfPaths);

		return numberOfPaths;
	}

	private Map<RDGNode, Integer> getNumberMapsOfPathVisited(RDGNode node) {
		Map<RDGNode, Boolean> marks = new HashMap<RDGNode, Boolean>();
		Map<RDGNode, Map<RDGNode, Integer>> cache = new HashMap<RDGNode, Map<RDGNode, Integer>>();

		return numPathsVisit(node, marks, cache);
	}

}
