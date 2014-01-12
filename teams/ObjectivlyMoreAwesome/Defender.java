package ObjectivlyMoreAwesome;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/**
 * 
 * @author Logan Esch, Andrew Wilson, Micheal Manning
 * @category Code for defensive soldiers.
 * 
 */
public class Defender extends Marine {

	static boolean enemiesClose = false;

	/**
	 * RUN<br>
	 * 
	 * Run Defender Code.
	 */
	public static void run() {
		try {
			/**
			 * Declare us to be a Defender for debugging
			 */
			rc.setIndicatorString(0, "Defender");

			

			rallyPt = hqLoc;

			/**
			 * TODO: We need to explore not using mines to attack, or in
			 * emergency. or to capture encampments
			 */
			if (status == 1) {// don't lay mines
				// move toward received goal, using swarm behavior
				swarmMove(rallyPt);
				// lay mines in a checkerboard pattern if status == 2
				// and there are no nearby enemies
			} else { // DON'T EVER DELETE THIS OR YOU WILL DIE!!!!
				// if not laying mines, (enemy close) use swarm behavior
				swarmMove(rallyPt);
			}
		} catch (Exception e) {
			System.out.println("Defender Exception");
			e.printStackTrace();
		}

	}

	/**
	 * SWARM MOVE<br>
	 * 
	 * Uses heuristics to determine the next location the robot should move to.
	 * 
	 * @param target
	 *            the goal location for the robot to move towards.
	 * @param allies
	 *            array of nearby allies.
	 * @param enemies
	 *            array of all enemies that can be sensed.
	 * @param nearbyEnemies
	 *            array of enemies near the robot.
	 * @param capturedEncamps
	 *            , int of how many encampments we have captured.
	 * @throws GameActionException
	 */
	protected static void swarmMove(MapLocation target)
			throws GameActionException {

		Direction toTarget = myLoc.directionTo(target);

		/**
		 * The weighting of target based on distance. Higher if farther.
		 */
		int targetWeighting = targetWeight(myLoc.distanceSquaredTo(target));

		/**
		 * The 'ideal' location for this robot to be in based on the heuristics.
		 */
		MapLocation goalLoc = myLoc.add(toTarget, targetWeighting);

		/**
		 * True if enemies are within vision range.
		 */

		/**
		 * True if any enemies have been spotted.
		 */
		boolean enemiesSpotted = false;

		if (enemies.length > 0) {
			enemiesSpotted = true;
		}
		if (nearbyEnemies.length > 0) {
			enemiesClose = true;
		}
		/*
		 * TODO weighted by the distance so that if close enough, it doesn't
		 * freak out.
		 */

		/**
		 * No enemies, spread out. Also true if a probe trying to capture
		 * encampments.
		 */
		if (enemies.length == 0) {
			// find closest allied robot. repel away from that robot.
			if (allies.length > 0) {
				MapLocation closestAlly = findClosestRobot(allies);
				goalLoc = goalLoc.add(myLoc.directionTo(closestAlly),
						peaceAlliedRepulsion);
			}

			/**
			 * We are out numbered, clump and run.
			 */
		} else if (allies.length < nearbyEnemies.length + outNumMargin
				&& myLoc.distanceSquaredTo(hqLoc) > 50) {
			// find closest allied robot. attract to that robot.
			if (allies.length > 0) {
				MapLocation closestAlly = findClosestRobot(allies);
				goalLoc = goalLoc.add(myLoc.directionTo(closestAlly),
						defAlliedAttraction);
			}
			// we need this check this because the array could still be null
			if (nearbyEnemies.length > 0) {// avoid enemy
				MapLocation closestEnemy = findClosestRobot(nearbyEnemies);
				/**
				 * If Enemy is on an encampment, he probably IS an encampment,
				 * Attack.
				 */
//				if (rc.senseEncampmentSquare(closestEnemy)) {
//					goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),
//							offNearEnemyAttraction);
//				} else {
					goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),
							defEnemyRepulsion);
//				}
			}

			/**
			 * We out number the enemy, clump and attack.
			 */
		} else if (allies.length >= (nearbyEnemies.length + outNumMargin)) {
			if (allies.length > 0) {
				MapLocation closestAlly = findClosestRobot(allies);
				goalLoc = goalLoc.add(myLoc.directionTo(closestAlly),
						offAlliedAttraction);
			}
			if (nearbyEnemies.length > 0) {
				MapLocation closestEnemy = findClosestRobot(nearbyEnemies);
				goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),
						offNearEnemyAttraction);
			} else {// no nearby enemies; go toward far enemy IF CLOSE TO HQ.
				MapLocation closestEnemy = findClosestRobot(enemies);
				if (hqLoc.distanceSquaredTo(closestEnemy) < 250) {
					goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),
							offFarEnemyAttraction);
				}
			}
		}
		/**
		 * find the direction to the goal location.
		 */
		Direction finalDir = myLoc.directionTo(goalLoc);
		// TODO figure out why this value is random, i don't think it should be.
		if (Math.random() < .1)
			finalDir = finalDir.rotateRight();

		/**
		 * move in that direction.
		 */
		simpleMove(finalDir, enemiesClose, enemiesSpotted);
	}

	/**
	 * TARGET WEIGHT<br>
	 * 
	 * Determines how badly we want to reach goal. <br>
	 * <br>
	 * 
	 * TODO figure out what this method does. i think it is to try and keep
	 * robots from going nuts out if they are to far away.
	 * 
	 * @param distSquared
	 *            The distance from the goal.
	 * @return int; Larger if we are farther from goal.
	 */
	protected static int targetWeight(int distSquared) {

		if (distSquared > 100) {
			return 3;
		} else if (distSquared > 9) {
			return 2;
		} else if (enemiesClose) {
			return 0;
		} else {

			return -5; // to make room for HQ to spawn.
		}
	}
}