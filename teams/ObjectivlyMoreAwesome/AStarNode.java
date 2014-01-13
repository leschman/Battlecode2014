package ObjectivlyMoreAwesome;

/**
 * 
 * @author Logan Esch, Andrew Wilson, Micheal Manning
 * 
 */
public class AStarNode {
	
	public int x, y, lowestCostSoFar, heuristicCostToGoal, combinedScore;
	public boolean onClosedList, onOpenList, isPassable;
	public AStarNode parent;
	
	public AStarNode(int x, int y, int lowestCostSoFar, int heuristicCostToGoal, boolean onClosedList, boolean onOpenList, boolean isPassable, AStarNode parent){
		this.x = x;
		this.y = y;
		this.lowestCostSoFar = lowestCostSoFar;
		this.heuristicCostToGoal = heuristicCostToGoal;
		combinedScore = lowestCostSoFar + heuristicCostToGoal;
		this.onOpenList = onOpenList;
		this.onClosedList = onClosedList;
		this.parent = parent;		
	}
}


