package team130;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class SoundTower extends RobotPlayer {
	/**
	 * Array of distances to shoot the noise. <br>
	 * <br>
	 * First shot should be "Small." <br>
	 * Rest should be "Large."<br>
	 */
	private static final int[] RANGE = { 20, 17, 11, 5 };
	private static int lastRangeShotIndex = 0;
	private static Direction directionLastShot = Direction.NORTH_WEST;

	public static void run() {
		try {

		} catch (Exception e) {
			System.out.println("Sound Tower Exception");
			e.printStackTrace();
		}

	}

	private static void shootNoise() {
		/**
		 * MapLocation to shoot next. 
		 */
		MapLocation target = null;

		distanceLoop: for (int i = lastRangeShotIndex; i < RANGE.length; i++) {
			if(i == 0){
				updateNextDirectionToShoot();
			}
			
			target = myLoc.add(directionLastShot, RANGE[i]);
			
			if (target.x < 0 || target.y < 0 || target.x > rc.getMapWidth()
					|| target.y > rc.getMapHeight()) {
				break distanceLoop;
			} else {
				
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
