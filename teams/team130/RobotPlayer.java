package team130;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class RobotPlayer {
	
	public static void run(RobotController rc){
		while(true){
			
		}
	}
	
	/**MAPLOCATION TO INT
	 * 
	 * @param maplocation the location to convert to an int.
	 * @return an integer representation of the MapLocation for broadcasting. 
	 */
	public static int mapLocationToInt(MapLocation maplocation){
		return (maplocation.x * 1000 + maplocation.y);
	}
	
	/**INT TO MAPLOCATION
	 * 
	 * @param loc int to convert to MapLocation
	 * @return a MapLocation that was represented by the int. 
	 */
	public static MapLocation intToMapLocation(int loc){
		int x = loc / 1000;
		int y = loc - (1000 * x);
		return new MapLocation(x,y);
	}

}
