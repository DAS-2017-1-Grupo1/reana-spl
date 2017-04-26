package tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopologicalSorting {

	/**
	 * Run {@link #topoSortVisit(RDGNode, Map, List)} to get one sort graph
	 *
	 * @param transitiveDependencies
	 */

	public void runTopologicalSortVisit(List<RDGNode> transitiveDependencies, RDGNode node) {
		Map<RDGNode, Boolean> marks = new HashMap<RDGNode, Boolean>();

		topoSortVisit(node, marks, transitiveDependencies);
	}

	/**
	 * Topological sort {@code visit} function (Cormen et al.'s algorithm).
	 * 
	 * @param node
	 * @param marks
	 * @param sorted
	 * @throws CyclicRdgException
	 */
	private void topoSortVisit(RDGNode node, Map<RDGNode, Boolean> marks, List<RDGNode> sorted)
			throws CyclicRdgException {
		if (isRunningTopologySort(node, marks)) {
			// Mark node temporarily (cycle detection)
			marks.put(node, false);

			for (RDGNode child : node.getDependencies()) {
				topoSortVisit(child, marks, sorted);
			}

			// Mark node permanently (finished sorting branch)
			marks.put(node, true);
			sorted.add(node);
		}
	}

	/**
	 * Auxiliar method to topological sort. See
	 * {@link #topoSortVisit(RDGNode, Map, List)}
	 *
	 */
	public boolean isRunningTopologySort(RDGNode node, Map<RDGNode, Boolean> marks) {
		boolean isRunning = false;

		if (hasAtLeastOneCycle(marks, node)) {
			// Visiting temporarily marked node -- this means a cyclic
			// dependency!
			throw new CyclicRdgException();
		} else if (!marks.containsKey(node)) {
			isRunning = true;
		}

		return isRunning;
	}

	private boolean hasAtLeastOneCycle(Map<RDGNode, Boolean> marks, RDGNode node) {
		return marks.containsKey(node) && marks.get(node) == false;
	}

}
