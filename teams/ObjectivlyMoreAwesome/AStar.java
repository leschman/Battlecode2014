package ObjectivlyMoreAwesome;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Stack;

public class AStar {

	/**
	 * array to keep track of cost to reach squares.
	 */
	static AStarNode[][] costArr;
	static MapLocation objective;
	static RobotController rc;

	public static MapLocation[] findAStarPath(MapLocation currentLoc,
			MapLocation goal, RobotController rc2) {

		// reiniatlize values to avoid garbage collection.

		objective = goal;
		rc = rc2;
		costArr = new AStarNode[rc.getMapWidth()][rc.getMapHeight()];

		// declare Comparator and Priority Queue.
		Comparator<MapLocation> comparator = new MapLocationComparator();
		PriorityQueue<MapLocation> openList = new PriorityQueue<>(10,
				comparator);

		// add the start location to the openList.
		openList.add(currentLoc);
		// start location has cost 0.
		costArr[currentLoc.x][currentLoc.y] = new AStarNode(currentLoc.x,
				currentLoc.y, 0, calculateHeuristic(currentLoc, goal), false,
				true, true, null);

		boolean pathFound = false; // done flag.
		ArrayList<MapLocation> closedList = new ArrayList<>();
		int mapHeight = rc.getMapHeight();
		int mapWidth = rc.getMapWidth();

		while (!pathFound) {
			// get the "best" next location from the heap.
			MapLocation loc = openList.poll();
			// add the adjacent squares to this spot to the heap.
			addAdjacentSquares(openList, loc, mapHeight, mapWidth);
			// add this location to the list of the path we want to follow.
			closedList.add(loc);
			if(loc.x == objective.x && loc.y == objective.y){
				pathFound = true;
			}
		}
		
		//PathFound 
		

		return (extractPath(closedList.remove(closedList.size() - 1)));

	}

	/**
	 * MAP LOCATION COMPARATOR comparator to determine the "best" location to
	 * try next.
	 * 
	 * @author Logan Esch, Andrew Wilson, Micheal Manning
	 * 
	 */
	private static class MapLocationComparator implements
			Comparator<MapLocation> {

		@Override
		public int compare(MapLocation o1, MapLocation o2) {
			
			if (costArr[o1.x][o1.y].combinedScore < costArr[o2.x][o2.y].combinedScore) {
				return -1;
			} else if (costArr[o1.x][o1.y].combinedScore > costArr[o2.x][o2.y].combinedScore) {
				return 1;
			} else {
				return 0;
			}

		}
	}

	/**
	 * CALCULATE HEURISTIC calculates the heuristic for use in the A* algorithm.
	 * 
	 * @param currentLoc
	 *            the location to calculate from.
	 * @param objective
	 *            the goal location to get to.
	 * @return the estimated cost of reaching the location as an int.
	 */
	private static int calculateHeuristic(MapLocation currentLoc,
			MapLocation objective) {
		int xDist = Math.abs(currentLoc.x - objective.x);
		int yDist = Math.abs(currentLoc.x - objective.y);
		return Math.max(xDist, yDist);
	}

	/**
	 * ADD ADJACENT SQUARES Adds adjacent squares to a given square to a
	 * PriorityQueue.
	 * 
	 * @param priorityQueue
	 *            the PriorityQueue to add the squares too.
	 * @param loc
	 *            the square to circle around.
	 * @param mapHeight
	 *            the height of the map.
	 * @param mapWidth
	 *            the width of the map.
	 * @param costToReachLoc
	 *            the cost to reach the loc square.
	 * @param costArr
	 *            two dimensional int array to keep track of cost to reach
	 *            squares.
	 */
	private static void addAdjacentSquares(
			PriorityQueue<MapLocation> priorityQueue, MapLocation loc,
			int mapHeight, int mapWidth) {
		for (int i = -1; i <= 1; i++) {
			inner: for (int j = -1; j <= 1; j++) {
				// check for bounds and also to make sure we don't re-add the
				// square.
				if ((i == 0 && j == 0) || loc.x + i < 0 || loc.y + j < 0
						|| loc.x + i > mapHeight || loc.y + j > mapWidth) {
					break inner;
				} else {
					MapLocation temp = new MapLocation(loc.x + i, loc.y + j);
					TerrainTile tempType = rc.senseTerrainTile(temp);
					if (tempType.equals(TerrainTile.VOID)) {
						// can't go there skip it.
						break inner;
					} else if (temp.equals(TerrainTile.ROAD)) {
						// roads cost .7 of normal tiles.
						if (costArr[temp.x][temp.y] == null
								|| costArr[temp.x][temp.y].lowestCostSoFar + 7 < costArr[temp.x][temp.y].lowestCostSoFar) {
							
							costArr[temp.x][temp.y] = new AStarNode(temp.x,
									temp.y,
									costArr[loc.x][loc.y].lowestCostSoFar + 7,
									calculateHeuristic(temp, objective), false,
									false, true, costArr[loc.x][loc.y]);
						}
					} else {
						if (costArr[temp.x][temp.y] == null
								|| costArr[temp.x][temp.y].lowestCostSoFar + 10 < costArr[temp.x][temp.y].lowestCostSoFar) {
							costArr[temp.x][temp.y] = costArr[temp.x][temp.y] = new AStarNode(temp.x,
									temp.y,
									costArr[loc.x][loc.y].lowestCostSoFar + 10,
									calculateHeuristic(temp, objective), false,
									false, true, costArr[loc.x][loc.y]);
						}
					}
					priorityQueue.add(temp);
				}
			}
		}
	}
	
	private static MapLocation[] extractPath(MapLocation loc){
		AStarNode node = costArr[loc.x][loc.y];
		Stack<MapLocation> stack = new Stack<>();
		while(node.parent!=null){
			stack.add(new MapLocation(node.x, node.y));
			node = node.parent;
		}
		return (MapLocation[])stack.toArray();
	}
}
