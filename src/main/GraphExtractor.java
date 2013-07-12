package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GraphExtractor {
	private int NUMBER_OF_COLORS = 10;
	private int MAX_ITERATIONS = 3;
	private int DEPTH = 3;
	
	private BufferedReader reader;
	
	private HashMap<Integer, List<Integer>> graph;
	private HashSet<Integer> nodesInGraph;

	private HashMap<Integer, Node> IDsToNodes;
	
	private int vertexCount;
	
	private int blankCount;
	
	public GraphExtractor(String path) throws IOException {
		
		reader = new BufferedReader(new FileReader(path));
		String line = null;
		
		IDsToNodes = new HashMap<Integer, Node>();
		graph = new HashMap<Integer, List<Integer>>();
		
		nodesInGraph = new HashSet<Integer>();
		
		
		HashMap<Integer, HashSet<Integer>> nodeToNeighbors = 
				new HashMap<Integer, HashSet<Integer>>();
		
		vertexCount = 0;
		
		while ((line = reader.readLine()) != null) {
			
			String[] parts = line.split("\\s");
			
			if (parts[0].equals("p")) {
				vertexCount = Integer.parseInt(parts[2]);
				// Initialize
				for (int i=0; i<vertexCount; i++) {
					
					List<Integer> listOfPossibleColors = new ArrayList<Integer>();
					for (int j=1; j<NUMBER_OF_COLORS+1; j++) {
						listOfPossibleColors.add(j);
					}
					
					graph.put(i, listOfPossibleColors);
					nodeToNeighbors.put(i, new HashSet<Integer>());
				}
			}
			else if (parts[0].equals("e")){
				// They use 1-indexing, I use 0-indexing, hence the minus one
				int vertex1id = Integer.parseInt(parts[1]) - 1;
				int vertex2id = Integer.parseInt(parts[2]) - 1;
				nodeToNeighbors.get(vertex1id).add(vertex2id);
				nodeToNeighbors.get(vertex2id).add(vertex1id);
			}
		}
		
		for (int i=0; i<vertexCount; i++) {
			IDsToNodes.put(i, new Node(nodeToNeighbors.get(i), i)); // Create all the nodes
			nodesInGraph.add(i);
		}
		
		reinitializeAllBonuses(graph);
		
		// THE ABOVE IS INITIALIZATION OF THE GRAPH
				
		HashMap<Integer, Integer> finalColoring = machineLearningGreedy(); // TODO
		
		System.out.println("COLORED GRAPH WITH " + blankCount + " blanks:");
		for (int i=0; i<vertexCount; i++) {
			System.out.println("Node " + i + " colored with color " + finalColoring.get(i));
		} 
		
//		System.out.println(countTriangles());
	}	
	
	// This function is optimized for coloring graphs that can be colored without blanks
	private HashMap<Integer, Integer> machineLearningGreedy() {
		HashMap<Integer, List<Integer>> pristine = deepGraphCopy(graph);
		HashMap<Integer, List<Integer>> masterCopy = deepGraphCopy(graph);
		HashMap<Integer, List<Integer>> graphClone;
		
		HashMap<Integer, Double> nodeIDtoTotalBlankNumSame = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> nodeIDtoNumColoringsSame = new HashMap<Integer, Integer>();
		
		HashMap<Integer, Double> nodeIDtoTotalBlankNumDiff = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> nodeIDtoNumColoringsDiff = new HashMap<Integer, Integer>();
		
		double nodeIDtoTotalBlankNumBlank = 0.0;
		int nodeIDtoNumColoringsBlank = 0;
		
		
		int realBlankCount = 0;
		
		double currentBlankNum;
		int currentNumColorings;
		
		HashMap<Integer, Integer> masterColoring = new HashMap<Integer, Integer>();
		
		colorIt(1, 0, masterCopy, masterColoring);
		
		// THESE ARE FOR queens10_10 ONLY
		
		colorIt(2, 1, masterCopy, masterColoring);
		colorIt(3, 2, masterCopy, masterColoring);
		colorIt(4, 3, masterCopy, masterColoring);
		colorIt(5, 4, masterCopy, masterColoring);
		colorIt(6, 5, masterCopy, masterColoring);
		colorIt(7, 6, masterCopy, masterColoring);
		colorIt(8, 7, masterCopy, masterColoring);
		colorIt(9, 8, masterCopy, masterColoring);
		colorIt(10, 9, masterCopy, masterColoring);
		colorIt(3, 10, masterCopy, masterColoring);
		colorIt(4, 11, masterCopy, masterColoring);
		colorIt(5, 12, masterCopy, masterColoring);
		colorIt(6, 13, masterCopy, masterColoring);
		colorIt(7, 14, masterCopy, masterColoring);
		colorIt(8, 15, masterCopy, masterColoring);
		colorIt(9, 16, masterCopy, masterColoring);
		colorIt(10, 17, masterCopy, masterColoring);
		colorIt(1, 18, masterCopy, masterColoring);
		colorIt(2, 19, masterCopy, masterColoring);
		
		while (!masterCopy.isEmpty()) {
			
			for (int i : pristine.keySet()) {
				// These two dicts are for how we do when the node-to-color is the same color as 
				// node i.
				nodeIDtoTotalBlankNumSame.put(i, 0.0);
				nodeIDtoNumColoringsSame.put(i, 0);
				
				// These two dicts are for how we do when the node-to-color is a different color 
				// from node i.
				nodeIDtoTotalBlankNumDiff.put(i, 0.0);
				nodeIDtoNumColoringsDiff.put(i, 0);
				
				// These two numbers are for how we do when the node-to-color is blank.
				nodeIDtoTotalBlankNumBlank = 0.0;
				nodeIDtoNumColoringsBlank = 0;
			}
			
			int IDofNodeToColor = randomChoose(masterCopy);
			HashMap<Integer, Integer> coloring;
			
			for (int i=0; i<100000; i++) {
				blankCount = 0;
				// Clone the partial graph of what's left
				graphClone = deepGraphCopy(masterCopy);
				coloring = randomGreedyColoring(graphClone);
				
				coloring.putAll(masterColoring);
				
				if (coloring.get(IDofNodeToColor) == 0) {
					nodeIDtoTotalBlankNumBlank += blankCount;
					nodeIDtoNumColoringsBlank += 1;
				}
				
				else {
					for (int j : pristine.keySet()) {
						
						if (coloring.get(j).equals(coloring.get(IDofNodeToColor))) {
							currentBlankNum = nodeIDtoTotalBlankNumSame.get(j);
							nodeIDtoTotalBlankNumSame.put(j, currentBlankNum + blankCount);
							
							currentNumColorings = nodeIDtoNumColoringsSame.get(j);
							nodeIDtoNumColoringsSame.put(j, currentNumColorings + 1);
						}
						
						else {
							currentBlankNum = nodeIDtoTotalBlankNumDiff.get(j);
							nodeIDtoTotalBlankNumDiff.put(j, currentBlankNum + blankCount);
							
							currentNumColorings = nodeIDtoNumColoringsDiff.get(j);
							nodeIDtoNumColoringsDiff.put(j, currentNumColorings + 1);
						}
					}
				}
			}
			
			double bestAvg;
			if (nodeIDtoNumColoringsBlank == 0) {
				// If the node-to-color was never blank in any of the random colorings, assign
				// a huge blankCount to making it blank.
				bestAvg = vertexCount;
			}
			else {
				bestAvg = nodeIDtoTotalBlankNumBlank / nodeIDtoNumColoringsBlank;
			}
						
			double avg;
			int bestColor = 0;
			
			double sumOfBlankNums = 0.0;
			int sumOfColorings = 0;
					
			for (int possibleColor : masterCopy.get(IDofNodeToColor)) {
				
				// for each candidate color, look at other nodes with that color, and
				// see if past colorings with both the node-to-be-colored and those nodes
				// being colored the same color did well.
				for (int nodeID : pristine.keySet()) {
					if (masterColoring.containsKey(nodeID)) {
						if (possibleColor == masterColoring.get(nodeID)) {
							if (nodeIDtoTotalBlankNumSame.containsKey(nodeID)) {
								sumOfBlankNums += nodeIDtoTotalBlankNumSame.get(nodeID);
								sumOfColorings += nodeIDtoNumColoringsSame.get(nodeID);
							}
						}
						
						else {
							if (nodeIDtoTotalBlankNumDiff.containsKey(nodeID)) {
								sumOfBlankNums += nodeIDtoTotalBlankNumDiff.get(nodeID);
								sumOfColorings += nodeIDtoNumColoringsDiff.get(nodeID);
							}
						}
					}
				}
				
				avg = sumOfBlankNums / sumOfColorings;
				System.out.println("" + IDofNodeToColor + " " + possibleColor + " " + avg);
				
				if (avg < bestAvg) {
					bestAvg = avg;
					bestColor = possibleColor;
				}
			}
			
			System.out.println("ASSIGNED COLOR " + bestColor + " TO NODE " + IDofNodeToColor);
			
			colorIt(bestColor, IDofNodeToColor, masterCopy, masterColoring);
			
			if (bestColor == 0) {
				realBlankCount += 1;
			}
		}	
		
		blankCount = realBlankCount;
		return masterColoring;
	}
	
	private HashMap<Integer, Integer> repeatRandomGreedy() {
		HashMap<Integer, Integer> bestColoringSoFar = null;
		int bestBlankCountSoFar = vertexCount;
		
		HashMap<Integer, List<Integer>> graphCopy = deepGraphCopy(graph);
		
		for (int i=0; i<100000; i++) {
			graph = deepGraphCopy(graphCopy);
			blankCount = 0;
			
			HashMap<Integer, Integer> candidateColoring = randomGreedyColoring(graph);
			if (blankCount < bestBlankCountSoFar) {
				bestBlankCountSoFar = blankCount;
				bestColoringSoFar = candidateColoring;
			}
		}
		blankCount = bestBlankCountSoFar;
		
		return bestColoringSoFar;
	}
	
	
	private HashMap<Integer, Integer> randomGreedyColoring(HashMap<Integer, List<Integer>> graph) {
		Random rand = new Random();
		HashMap<Integer, Integer> coloring = new HashMap<Integer, Integer>();
		
		while (!graph.isEmpty()) {
			int randomNodeID = randomChoose(graph);
			
			int color;
			if (graph.get(randomNodeID).isEmpty()) {
				color = 0;
				blankCount++;
			}
			else {
				color = graph.get(randomNodeID).get(rand.nextInt(graph.get(randomNodeID).size()));
	//			color = graph.get(randomNodeID).get(0);
			}
			
			coloring.put(randomNodeID, color);
			purgeNeighborsOfColor(color, randomNodeID, IDsToNodes, graph);
			
			graph.remove(randomNodeID);
		}
		
		return coloring;
	}
	
	private HashMap<Integer, Integer> findColoring() {
		
		Random rand = new Random();
		HashMap<Integer, Integer> coloring = new HashMap<Integer, Integer>();
		
		for (int inclusionVertexID=0; inclusionVertexID<vertexCount; inclusionVertexID++) {
			
			reinitializeAllBonuses(graph);
			
			Node nodeToColor = IDsToNodes.get(inclusionVertexID);
			
			for (int iterationNum = 0; iterationNum < MAX_ITERATIONS; iterationNum++) {
			
				for (int vertexID=inclusionVertexID; vertexID<vertexCount; vertexID++) {
					
					Node nodeToUpdate = IDsToNodes.get(vertexID);
					
					for (int color : graph.get(vertexID)) {
				
						double thisBonus = nodeToUpdate.approximateBonus(IDsToNodes, graph, color, DEPTH);
						
/*						if ((iterationNum < MAX_ITERATIONS / 2) && (rand.nextInt(4) == 0)) {
							nodeToUpdate.bonusEstimate.put(color, Math.min(1.0 - thisBonus, 1.0));
						} */
						
//						else {
							nodeToUpdate.bonusEstimate.put(color, thisBonus);
//						}
					}
				}
				
				resetAllBonuses();
			}
			
			Iterator<Integer> colorSetIterator = nodeToColor.bonusEstimate.keySet().iterator();
			
			double bestBonusSoFar = 0.0;
			int bestColorSoFar = 0;
			
			while (colorSetIterator.hasNext()) {
				int nextColor = colorSetIterator.next();
				
				double thisBonus = nodeToColor.bonusEstimate.get(nextColor);
				
				if (thisBonus >= bestBonusSoFar) {
					bestBonusSoFar = thisBonus;
					bestColorSoFar = nextColor;
				}
			}
			
			System.out.println("ASSIGNED COLOR " + bestColorSoFar + " TO NODE " + inclusionVertexID);
			coloring.put(inclusionVertexID, bestColorSoFar);
			if (bestColorSoFar == 0) {
				blankCount++;
			}
		
			purgeNeighborsOfColor(bestColorSoFar, inclusionVertexID, IDsToNodes);
		
			graph.remove(inclusionVertexID);
		}
		
		return coloring;
	}
	
	public void purgeNeighborsOfColor(int color, int vertexID, HashMap<Integer, Node> IDsToNodes) {
		HashSet<Integer> neighbors = IDsToNodes.get(vertexID).neighbors;
		
		for (int neighborID : neighbors) {
			if (graph.containsKey(neighborID)) {
				graph.get(neighborID).remove((Object) color);
				IDsToNodes.get(neighborID).bonusEstimate.remove(color);
			}
		}
	}
	
	public void purgeNeighborsOfColor(int color, int vertexID, HashMap<Integer, Node> IDsToNodes,
			HashMap<Integer, List<Integer>> graph) {
		HashSet<Integer> neighbors = IDsToNodes.get(vertexID).neighbors;
		
		for (int neighborID : neighbors) {
			if (graph.containsKey(neighborID)) {
				graph.get(neighborID).remove((Object) color);
				IDsToNodes.get(neighborID).bonusEstimate.remove(color);
			}
		}
	}
	
	public void colorIt(int color, int vertexID, HashMap<Integer, List<Integer>> graph,
			HashMap<Integer, Integer> coloring) {
		purgeNeighborsOfColor(color, vertexID, IDsToNodes, graph);
		coloring.put(vertexID, color);
		graph.remove(vertexID);
	}
	
	
	public void resetAllBonuses() {
		for (int nodeID : graph.keySet()) {
			IDsToNodes.get(nodeID).resetBonus();
		}
	}
	
	public void reinitializeAllBonuses(HashMap<Integer, List<Integer>> graph) {
		for (int i=0; i<vertexCount; i++) {
			if (graph.containsKey(i)) {
				IDsToNodes.get(i).reinitializeBonus(graph, IDsToNodes);
			}
		}
	}
	
	public <E> E randomChoose(HashSet<E> set) {
		@SuppressWarnings("unchecked")
		E[] choiceArray = (E[]) set.toArray();
		Random rand = new Random();
		return choiceArray[rand.nextInt(choiceArray.length)];
	}
	
	public <K, V> K randomChoose(HashMap<K, V> map) {
		@SuppressWarnings("unchecked") 
		K[] choiceArray = (K[]) map.keySet().toArray();
		Random rand = new Random();
		return choiceArray[rand.nextInt(choiceArray.length)];
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
	
    public static void main(String[] args) {
    	try {
    		// TODO
//			new GraphExtractor("/Users/adam/Documents/workspace/independent_set/frb30-15-1.mis");
    		new GraphExtractor("/Users/adam/Documents/workspace/independent_set/mygraph.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
