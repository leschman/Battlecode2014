package team130;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class SoundTower extends RobotPlayer {
	/**
	 * Array of distances to shoot the noise. <br>
	 * <br>
	 * First shot should be "Small." <br>
	 * Rest should be "Large."
	 */
	private static final int[] RANGE = { 20, 17, 11, 5 };

	/**
	 * Variable to track the index of the range last shot.
	 */
	private static int lastRangeShotIndex = 0;

	/**
	 * Variable to track direction last shot.
	 */
	private static Direction directionLastShot = Direction.NORTH_WEST;

	public static void run() {
		try {
			if(rc.isActive()){
				shootNoise();
				//reset this variable. 
				if(lastRangeShotIndex == RANGE.length){
					lastRangeShotIndex = 0;
				}
			}

		} catch (Exception e) {
			System.out.println("Sound Tower Exception");
			e.printStackTrace();
		}

	}

	private static void shootNoise() throws GameActionException {
		/**
		 * MapLocation to shoot next.
		 */
		MapLocation target = null;

		distanceLoop: for (; lastRangeShotIndex < RANGE.length; lastRangeShotIndex++) {

			// we've fired at all distances in this direction, change
			// direction.
			if (lastRangeShotIndex == 0) {
				updateNextDirectionToShoot();
			}

			// this is the location we want to try to shoot.
			target = myLoc.add(directionLastShot, RANGE[lastRangeShotIndex]);

			// we need to make sure the target locaiton is within bounds.
			if (target.x < 0 || target.y < 0 || target.x > rc.getMapWidth()
					|| target.y > rc.getMapHeight()) {
				// target out of bounds, restart loop.
				break distanceLoop;
			} else {
				// target in bounds. Fire.
				if (lastRangeShotIndex == 0) {
					rc.attackSquareLight(target);
				} else {
					rc.attackSquare(target);
				}
				
				//after we have shot, yield. 
				rc.yield();
			}
		}
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

}
