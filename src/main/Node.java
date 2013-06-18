package main;

import java.util.*;

public class Node {
	public Set<Integer> neighbors;
	public int id;
	
	public int color; // color = 0 is BLANK
	
	public HashMap<Integer, Integer> bonusEstimate;
	
	public int NUMBER_OF_COLORS = 2; // this counts only legit colors
	
	public int INFINITY = 100000;
	public int NEGATIVE_INFINITY = -1 * INFINITY;
	
	public Node(HashSet<Integer> neighbors, int id) {
		this.neighbors = neighbors;
		this.id = id;
		bonusEstimate = new HashMap<Integer, Integer>();
		for (int i=1; i<NUMBER_OF_COLORS+1; i++) {
			bonusEstimate.put(i, 1);
		}
	}
	
	
	// graph is a mapping from node IDs in the graph to the list of legal colors for that 
	// node (not counting the blank color)
	public double approximateBonus(HashMap<Integer, Node> IDsToNodes, HashMap<Integer, List<Integer>> graph,
			int color, int depth) { 
				
		List<Integer> listOfPossibleColors = graph.get(id);
		
		if (!listOfPossibleColors.contains(color)) {
			throw new RuntimeException("Color " + color + " is not in the list " +
					"of possible colors of node " + id + "!");
		}
		
		if (depth == 0) {
			return bonusEstimate.get(color);
		}
		
		// Check that the set of neighbors is not empty; if so, return base case
		@SuppressWarnings("unchecked")
		HashMap<Integer, List<Integer>> graphThatICanMangle = (HashMap<Integer, List<Integer>>) graph.clone();
		
		Set<Integer> nodesInGraph = graphThatICanMangle.keySet();
		
		nodesInGraph.retainAll(neighbors);
		if (nodesInGraph.isEmpty()) {
			if (graph.get(id).size() == 0) {
				System.out.println("Graph: " + graph + " Node: " + id + " Color: " + color + " Bonus: 0.0");
				return 0.0;
			}
			else {
				System.out.println("Graph: " + graph + " Node: " + id + " Color: " + color + " Bonus: 1.0"); 
				return 1.0;
			}
		}
		
		else {
			HashMap<Integer, List<Integer>> graphCopy = deepGraphCopy(graph);
			
			graphCopy.remove(id);
			
			double returnValue = 1.0;
			Iterator<Integer> neighborIterator = neighbors.iterator();
			while (neighborIterator.hasNext()) {
								
				int nextNeighborID = neighborIterator.next();
				
				if (graphCopy.containsKey(nextNeighborID)) {
					
					double bestBonusValueWithoutUsingColor = 0; 
					double bonusValueUsingColor = 0; 
					
					for (int i : graphCopy.get(nextNeighborID)) {
						double bonusValue;
						
//						System.out.println(graphCopy);
//						System.out.println(graphCopy.containsKey(nextNeighborID));
						
						bonusValue = IDsToNodes.get(nextNeighborID).approximateBonus(IDsToNodes, 
								graphCopy, i, depth - 1);
						
						if (i == color) {
							bonusValueUsingColor = bonusValue;
						}
						
						else {
							if (bonusValue > bestBonusValueWithoutUsingColor) {
								bestBonusValueWithoutUsingColor = bonusValue;
							}
						}
					}
					
					returnValue -= Math.max(bonusValueUsingColor - bestBonusValueWithoutUsingColor, 0);
					
					removeColorFromListOfPossibleForgiving(color, nextNeighborID, graphCopy);
				}	
			}
			
			System.out.println("Graph: " + graph + " Node: " + id + " Color: " + color + " Bonus: " + returnValue);
			
			return returnValue;
		}
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Integer, List<Integer>> deepGraphCopy(HashMap<Integer, List<Integer>> graph) {
		HashMap<Integer, List<Integer>> returnMap = new HashMap<Integer, List<Integer>>();
		
		Set<Integer> originalKeySet = graph.keySet();
		Iterator<Integer> originalKeySetIterator = originalKeySet.iterator();
		
		while (originalKeySetIterator.hasNext()) {
			int nextKey = originalKeySetIterator.next();
			returnMap.put(nextKey, (List<Integer>) ((ArrayList<Integer>) graph.get(nextKey)).clone());
		}
		
		return returnMap;
	}
	
	public void removeColorFromListOfPossibleUnforgiving(int color, int nodeID,
			HashMap<Integer, List<Integer>> graph) {
		if (graph.get(nodeID).contains(color)) {
			graph.get(nodeID).remove((Object) color);
		}
		
		else {
			throw new RuntimeException("Node " + nodeID + " doesn't contain color " + color);
		}
	}
	
	public void removeColorFromListOfPossibleForgiving(int color, int nodeID,
			HashMap<Integer, List<Integer>> graph) {

		graph.get(nodeID).remove((Object) color);
	}
	
    public static void main(String[] args) {
    	/*
    	HashSet<Integer> node1neighbors = new HashSet<Integer>();
    	node1neighbors.add(2);
    	node1neighbors.add(3);
    	
    	HashSet<Integer> node2neighbors = new HashSet<Integer>();
    	node2neighbors.add(1);
    	node2neighbors.add(3);
    	node2neighbors.add(4);
    	
    	HashSet<Integer> node3neighbors = new HashSet<Integer>();
    	node3neighbors.add(1);
    	node3neighbors.add(2);
    	node3neighbors.add(4);
    	
    	HashSet<Integer> node4neighbors = new HashSet<Integer>();
    	node4neighbors.add(2);
    	node4neighbors.add(3);
    	
    	Node node1 = new Node(node1neighbors, 1);
    	Node node2 = new Node(node2neighbors, 2);
    	Node node3 = new Node(node3neighbors, 3);
    	Node node4 = new Node(node4neighbors, 4);
    	
    	HashMap<Integer, Node> IDsToNodes = new HashMap<Integer, Node>();
    	
    	IDsToNodes.put(1, node1);
    	IDsToNodes.put(2, node2);
    	IDsToNodes.put(3, node3);
    	IDsToNodes.put(4, node4);
    	
    	HashMap<Integer, Boolean> graph = new HashMap<Integer, Boolean>();
    	
    	graph.put(1, true);
    	graph.put(2, true);
    	graph.put(3, true);
    	graph.put(4, true);
    	*/
    	
    	HashSet<Integer> node1neighbors = new HashSet<Integer>();
    	node1neighbors.add(2);
    	node1neighbors.add(3);
    	
    	HashSet<Integer> node2neighbors = new HashSet<Integer>();
    	node2neighbors.add(1);
    	node2neighbors.add(2);
    	
    	HashSet<Integer> node3neighbors = new HashSet<Integer>();
    	node3neighbors.add(1);
    	node3neighbors.add(2);
    	node3neighbors.add(4);
    	node3neighbors.add(5);
    	
    	HashSet<Integer> node4neighbors = new HashSet<Integer>();
    	node4neighbors.add(3);
    	node4neighbors.add(5);
    	
    	HashSet<Integer> node5neighbors = new HashSet<Integer>();
    	node5neighbors.add(4);
    	node5neighbors.add(5);
    	
    	Node node1 = new Node(node1neighbors, 1);
    	Node node2 = new Node(node2neighbors, 2);
    	Node node3 = new Node(node3neighbors, 3);
    	Node node4 = new Node(node4neighbors, 4);
    	Node node5 = new Node(node5neighbors, 5);
    	
    	HashMap<Integer, Node> IDsToNodes = new HashMap<Integer, Node>();
    	
    	IDsToNodes.put(1, node1);
    	IDsToNodes.put(2, node2);
    	IDsToNodes.put(3, node3);
    	IDsToNodes.put(4, node4);
    	IDsToNodes.put(5, node5);
    	
    	HashMap<Integer, List<Integer>> graph = new HashMap<Integer, List<Integer>>();
    	
    	List<Integer> listOfPossibleColors1 = new ArrayList<Integer>();
    	listOfPossibleColors1.add(1);
    	listOfPossibleColors1.add(2);
    	
    	List<Integer> listOfPossibleColors2 = new ArrayList<Integer>();
    	listOfPossibleColors2.add(1);
    	listOfPossibleColors2.add(2);
    	
    	List<Integer> listOfPossibleColors3 = new ArrayList<Integer>();
    	listOfPossibleColors3.add(1);
    	listOfPossibleColors3.add(2);
    	
    	List<Integer> listOfPossibleColors4 = new ArrayList<Integer>();
    	listOfPossibleColors4.add(1);
    	listOfPossibleColors4.add(2);
    	
    	List<Integer> listOfPossibleColors5 = new ArrayList<Integer>();
    	listOfPossibleColors5.add(1);
    	listOfPossibleColors5.add(2);
    	
    	graph.put(1, listOfPossibleColors1);
    	graph.put(2, listOfPossibleColors2);
    	graph.put(3, listOfPossibleColors3);
    	graph.put(4, listOfPossibleColors4);
    	graph.put(5, listOfPossibleColors5);
    	
    	System.out.println("g" + graph);
    	System.out.println(node3.approximateBonus(IDsToNodes, graph, 1, 100000));
    }
}
