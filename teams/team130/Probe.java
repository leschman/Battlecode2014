package team130;

import java.util.Random;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public class Probe extends Soldier {

	// create a new random
	static Random random = new Random(rc.getRobot().getID());

	public static void run(){
		try{
			//set goal to a corner.
			if(rallyPt.equals(hqLoc)){
				rallyPt = pickACorner();
			}
			if(targetWeight(myLoc.distanceSquaredTo(rallyPt)) == 3){
				rc.construct(RobotType.NOISETOWER);
			}else{
			swarmMove(rallyPt);
			}
			
			
		}catch(Exception e){
			System.out.println("Probe Exception");
			e.printStackTrace();
		}
	}

	private static MapLocation pickACorner() {
		float choice = random.nextFloat();

		if (choice < .25) {
			// go to top left
			return new MapLocation(0, 0);
		} else if (choice < .5) {
			// go to top right
			return new MapLocation(rc.getMapWidth(), 0);
		} else if (choice < .75) {
			// go to bottom right
			return new MapLocation(rc.getMapWidth(), rc.getMapHeight());
		} else {
			// go to bottom left
			return new MapLocation(0, rc.getMapHeight());
		}
	}
}
