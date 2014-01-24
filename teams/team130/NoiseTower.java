package team130;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class NoiseTower extends RobotPlayer {
	/**
	 * Array of distances to shoot the noise. <br>
	 * <br>
	 * First shot should be "Small." <br>
	 * Rest should be "Large."
	 */
	private static int[] RANGE = { 17, 15, 13, 11, 9, 7, 5, 0, -17, -15, -11,
			-9, -7, -5, 0 };
	/**
	 * boolean to track if location has been broadcast and is registered. 
	 */
	private static boolean locationRegistered = false;
	
	/**
	 * Variable to track direction last shot.
	 */
	private static Direction directionLastShot = Direction.NORTH_WEST;

	public static void run() {
		try {
			myLoc = rc.getLocation();
			if (rc.isActive()) {
				int channel = checkLocationRegistered();
				if(!locationRegistered){
					rc.broadcast(channel, mapLocationToInt(myLoc));;
				}
				shootNoise();
			}

		} catch (Exception e) {
			System.out.println("Sound Tower Exception");
			e.printStackTrace();
		}

	}

	private static void shootNoise() throws GameActionException {

		for (int x = 0; x < RANGE.length; x++) {
			int targetX = myLoc.x + RANGE[x];
			if (targetX >= 0 && targetX < rc.getMapWidth()) {
				for (int y = 0; y < RANGE.length; y++) {
					int targetY = myLoc.y + RANGE[y];
					if (targetY >= 0 && targetY < rc.getMapHeight()) {
						if ((RANGE[x] * RANGE[x]) + (RANGE[y] * RANGE[y]) < 300) {
							// inbounds, fire.
							if (targetX != myLoc.x && targetY != myLoc.y) {
								if (rc.getActionDelay() > 0) {
									actionDelayWait();
								}
								rc.attackSquare(new MapLocation(targetX,
										targetY));

								rc.yield();
							}
						}
					}
				}
			}
		}

		// distanceLoop: for (; lastRangeShotIndex < RANGE.length;
		// lastRangeShotIndex++) {
		//
		// // we've fired at all distances in this direction, change
		// // direction.
		// if (lastRangeShotIndex == 0) {
		// updateNextDirectionToShoot();
		// }
		//
		// // this is the location we want to try to shoot.
		// System.out.println("lastRangeShotIndex: " + lastRangeShotIndex);
		// System.out.println("directionLastShot : " + directionLastShot);
		// System.out.println("Range: " + RANGE[lastRangeShotIndex]);
		// try {
		// target = myLoc
		// .add(directionLastShot, RANGE[lastRangeShotIndex]);
		// } catch (NullPointerException e) {
		// lastRangeShotIndex++;
		// break distanceLoop;
		// }
		// System.out.println("got past throwing error");
		// // we need to make sure the target location is within bounds.
		// if (target.x < 0 || target.y < 0 || target.x > rc.getMapWidth()
		// || target.y > rc.getMapHeight()) {
		// // target out of bounds, restart loop.
		// lastRangeShotIndex++;
		// break distanceLoop;
		// } else {
		// // target in bounds. Fire.
		// if (lastRangeShotIndex == 0) {
		// rc.attackSquareLight(target);
		// } else {
		// rc.attackSquare(target);
		// }
		//
		// // after we have shot, yield.
		// rc.yield();
		// }
		// }
	}

	private static void updateNextDirectionToShoot() {
		/**
		 * Figure out next direction to shoot. presently start shooting north
		 * and then rotate clockwise around.
		 */
		if (directionLastShot == Direction.NORTH_WEST) {
			directionLastShot = Direction.NORTH;
		} else {
			directionLastShot = Direction.values()[directionLastShot.ordinal() + 1];
		}
	}

	private static void actionDelayWait() {
		while (rc.getActionDelay() > 0) {
			rc.yield();
		}
	}
	
	private static int checkLocationRegistered() throws GameActionException{
		//set false while we check. 
		locationRegistered = false;
		int myLocInt = mapLocationToInt(myLoc);
		int lastOpenChannel = -1;
		
		for(int i = 0; i < 20; i++){
			int broadcast = rc.readBroadcast(i);
			if(myLocInt == broadcast){
				//location has been registered. 
				locationRegistered = true;
				return (-1);
			}else if(lastOpenChannel == -1 && broadcast == 0){
				lastOpenChannel = i;
			}
		}
		return lastOpenChannel;
	}

}
