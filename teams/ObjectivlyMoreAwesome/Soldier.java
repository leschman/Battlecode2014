package ObjectivlyMoreAwesome;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;

/**
 * 
 * @author Logan Esch, Andrew Wilson, Micheal Manning
 * @category Base class for soldier robots.
 * 
 */
public class Soldier extends RobotPlayer {
	/**
	 * HEURISTICS FOR SWARM MIND TODO: optimize these heuristics.
	 */
	/**
	 * Attraction from neutral or enemy encamps when no enemies are around.
	 */
	final static int peaceEncampAttraction = 0;

	/**
	 * Repulsion from allied units when no enemies are around.
	 */
	final static int peaceAlliedRepulsion = -1;

	/**
	 * The margin we need to out-number enemy by in order to attack.
	 */
	final static int outNumMargin = 2;

	/**
	 * Attraction from allied units when out-numbering enemies are around.
	 */
	final static int defAlliedAttraction = 5;

	/**
	 * Repulsion from enemy units when there's more of them than allied.
	 */
	final static int defEnemyRepulsion = -10;

	/**
	 * Attraction from allied units when we out-number enemy.
	 */
	final static int offAlliedAttraction = 5;

	/**
	 * Attraction from near enemy units when we out-number enemy.
	 */
	final static int offNearEnemyAttraction = 10;

	/**
	 * Attraction from far enemy units when we out-number enemy.
	 */
	final static int offFarEnemyAttraction = 10;

	/**
	 * Variable to control vision used for swarm behavior. <br>
	 * <br>
	 * Should be 14 when we don't have VISON UPGRADE. <br>
	 * Should be 33 when we have VISION UPGRADE.
	 */
	static int vision = 14;
	/**
	 * Robot's type, <br>
	 * <br>
	 * -1: Uninitialized. <br>
	 * 0: Marine. <br>
	 * 1: SCV. <br>
	 * 2: Probe.
	 */
	static Type type = Type.UNINITALIZED;

	/**
	 * The rally point we are heading for.
	 */
	static MapLocation rallyPt;

	/**
	 * Status for mine laying.<br>
	 * <br>
	 * 1: don't lay mines.<br>
	 * 2: lay mines.
	 */
	static int status = 1;

	/**
	 * Array of all encampments.
	 */
	static MapLocation[] friendlyPASTRs;

	/**
	 * The encampment closest to the robot.
	 */
	static MapLocation encampClosest;

	/**
	 * The farthest encampment from the enemy HQ.
	 */
	static MapLocation encampFarthestFromEnemy;

	/**
	 * Array of allied robots within vision range.
	 */
	static Robot[] allies;

	/**
	 * Array of all enemy robots that our team can see.
	 */
	static Robot[] enemies;

	/**
	 * Array of enemy robots within vision range.
	 */
	static Robot[] nearbyEnemies;

	/**
	 * Array of encampments within vision range.
	 */
	static MapLocation[] nearbyEncamps;

	/**
	 * 
	 * Enumerator of robot soldier types.<br>
	 * <br>
	 * MARINE: offensive soldier.<br>
	 * DEFENDER: base defender. <br>
	 * PROBE: encampment capturer.<br>
	 * UNINITALIZED: type not set yet.
	 */
	protected static enum Type {
		MARINE, DEFENDER, PROBE, UNINITALIZED

	}

	/**
	 * Run soldier Code.
	 * 
	 * @throws GameActionException
	 */
	public static void run() throws GameActionException {

		/**
		 * Initialize locations.
		 */
		if (hqLoc == null) {
			hqLoc = rc.senseHQLocation();
		}
		if (enemyHQLoc == null) {
			enemyHQLoc = rc.senseEnemyHQLocation();
		}
		rallyPt = hqLoc;
		friendlyPASTRs = rc.sensePastrLocations(rc.getTeam());

		while (true) {
			try {
				if (rc.isActive()) {
					/**
					 * Initialize location.
					 */
					myLoc = rc.getLocation();

					/**
					 * Update Channel.
					 */
					channel = getChannel();

					/**
					 * Determine what type we are if we don't know.
					 */
					if (type == Type.UNINITALIZED) {
						int msg = rc.readBroadcast(channel + 2);
						type = whatTypeAmI(msg);
					}

					/**
					 * See what is around us.
					 */
					allies = rc.senseNearbyGameObjects(Robot.class, vision,
							rc.getTeam());
					enemies = rc.senseNearbyGameObjects(Robot.class, 10000000,
							rc.getTeam().opponent());
					nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,
							vision, rc.getTeam().opponent());

					/**
					 * Based on robot type run the code for that robot.
					 */
					switch (type) {
					case MARINE:
						Marine.run();
						break;
					case DEFENDER:
						Defender.run();
						break;

					default:
						/**
						 * Shouldn't ever happen but just in case.
						 */
						whatTypeAmI(Clock.getRoundNum());
						System.out.println("ROBOT DIDNT HAVE CODE TO RUN");
					}

				}
			} catch (Exception e) {
				System.out.println("Soldier Exception");
				e.printStackTrace();
			}
			rc.yield();
		}

	}

	/**
	 * WHAT TYPE AM I<br>
	 * 
	 * A personality test driven by selective reproduction and an engineered
	 * society mechanics.<br>
	 * <br>
	 * 
	 * int; Selects a marine or a probe. <br>
	 * 0: Marine. <br>
	 * 2: Probe. <br>
	 */
	private static Type whatTypeAmI(int numUnitsMade) {
		int charlesJungTest = (numUnitsMade % 10);
		if (charlesJungTest > 4) {
			type = Type.MARINE; // Marine
			// System.out.println("Made Marine");

		} else if (2 < charlesJungTest && charlesJungTest <= 4) {
			type = Type.DEFENDER;
		} else if (charlesJungTest <= 2) {
			type = Type.PROBE; // Probe
			// System.out.println("Made Probe");
		} else {
			type = Type.MARINE;
		}
		return type;
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
		boolean enemiesClose = false;

		/**
		 * True if any enemies have been spotted.
		 */
		boolean enemiesSpotted = false;

		if (enemies.length > 0) {
			enemiesSpotted = true;
		}
		/*
		 * TODO weighted by the distance so that if close enough, it doesn't
		 * freak out.
		 */

		/**
		 * No enemies, spread out. Also true if a probe trying to capture
		 * encampments.
		 */
		if (enemies.length == 0 || type == Type.PROBE) {
			// find closest allied robot. repel away from that robot.
			if (allies.length > 0) {
				// if (nearbyEncamps.length > 0) {
				// MapLocation closestEncamp =
				// findClosestMapLocation(nearbyEncamps);
				// goalLoc = goalLoc.add(
				// myLoc.directionTo(closestEncamp),
				// /**
				// * TODO Balance below, each additional encampment is
				// * more expensive.
				// */
				// peaceEncampAttraction
				// - (int) (rc.senseCaptureCost() - rc
				// .getTeamPower()));
				// }

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

				goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),
						defEnemyRepulsion);

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
			} else {// no nearby enemies; go toward far enemy
				MapLocation closestEnemy = findClosestRobot(enemies);
				goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),
						offFarEnemyAttraction);
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
			return 5;
		} else if (distSquared > 9) {
			return 3;
		} else {
			return 1; // tried changing to 0 from 1, need to test effect.
		}
	}

	/**
	 * SIMPLE MOVE<br>
	 * 
	 * Takes direction and determines if we want to build an encampment, defuse
	 * mines or move in that direction.
	 * 
	 * @param dir
	 *            direction we are trying to move in.
	 * @param enemiesClose
	 *            boolean to indicate enemies are close
	 * @param enemySpotted
	 *            boolean to indicate we have seen any enemies.
	 * @throws GameActionException
	 */
	protected static void simpleMove(Direction dir, boolean enemiesClose,
			boolean enemySpotted) throws GameActionException {

		/**
		 * see if we can move in any direction up to 90' off goal.
		 */
		int[] directionOffsets = { 0, 1, -1, 2, -2 };
		/**
		 * 0 straight, 1 Right 45', -1 Left 45', 2 Right 90', -2 Left 90'
		 */
		Direction lookingAtCurrently = null;
		lookAround: for (int d : directionOffsets) {
			lookingAtCurrently = Direction.values()[(dir.ordinal() + d + 8) % 8];
			/**
			 * check if we can move the direction we are looking.
			 */
			if (rc.canMove(lookingAtCurrently)) {

				if (!enemiesClose) {
					rc.move(lookingAtCurrently);
					break lookAround;
				}
				// TODO fix this so we avoid mines when enemies are
				// close
			} else {
				rc.move(lookingAtCurrently);
				break lookAround;
			}
		}
		// }
	}

	/**
	 * FIND CLOSEST ROBOT<br>
	 * 
	 * Returns the closest robot from an array.
	 * 
	 * @param robots
	 *            array of robots to search through.
	 * @return the location of the closest robot.
	 * @throws GameActionException
	 */
	protected static MapLocation findClosestRobot(Robot[] robots)
			throws GameActionException {
		int closestDist = 1000000;
		MapLocation closestEnemy = null;
		for (int i = robots.length; --i >= 0;) { // optimized for loop :D
			Robot arobot = robots[i];
			if (rc.canSenseObject(arobot)) {
				RobotInfo arobotInfo = rc.senseRobotInfo(arobot);
				int dist = arobotInfo.location.distanceSquaredTo(myLoc);
				if (dist < closestDist) {
					closestDist = dist;
					closestEnemy = arobotInfo.location;
				}
			}
		}
		return closestEnemy;
	}

	/**
	 * FIND CLOSEST MAP LOCATION<br>
	 * 
	 * Returns the closest MapLocation from an array.
	 * 
	 * @param locationsArr
	 *            array of map locations to search
	 * @return the closest location to the array.
	 * @throws GameActionException
	 */
	protected static MapLocation findClosestMapLocation(
			MapLocation[] locationsArr) throws GameActionException {
		int closestDist = 1000000;
		MapLocation closestLoc = null;
		for (int i = locationsArr.length; --i >= 0;) { // optimized for loop.
														// save dem bytecodes.
			MapLocation aLocation = locationsArr[i];
			int dist = aLocation.distanceSquaredTo(myLoc);
			if (dist < closestDist) {
				closestDist = dist;
				closestLoc = aLocation;
			}
		}
		return closestLoc;
	}

	/**
	 * ARTILLERY PLACEMENT<br>
	 * Determines if current location between our HQ and the Enemy HQ.
	 * 
	 * @return boolean true if good spot for artillery, false if not.
	 */
	protected static boolean goodArtillery() {
		int y = hqLoc.y - myLoc.y;
		int x = hqLoc.x - myLoc.x;
		int slopeX = hqLoc.x - enemyHQLoc.x;
		int slopeY = hqLoc.y - enemyHQLoc.y;
		int hqDistToEnemyHQ = hqLoc.distanceSquaredTo(enemyHQLoc);

		// only build arty if it is between our hq and enemy hq.
		if (myLoc.distanceSquaredTo(enemyHQLoc) < hqDistToEnemyHQ) {

			final int offset = 5; // determines how big of a swath of arty.
			for (int i = -offset; i < offset; i++) {
				if (slopeX == 0) {
					// Divide by 0.
					return true;
				}
				if ((y + i) == (slopeY / slopeX) * (x)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * FILTER ENCAMPS<br>
	 * Removes friendly encampments from an array.
	 * 
	 * @param allEncampments
	 *            All the encampments on the map.
	 * @param alliedEncampments
	 *            All the encampments we have captured.
	 * @return MapLocation[]; Containing encampments that we don't own.
	 * @throws GameActionException
	 */
	public static MapLocation[] filterEncamps(MapLocation[] allEncampments,
			MapLocation[] alliedEncampments) throws GameActionException {

		// locate uncaptured encampments within a certain radius
		MapLocation[] neutralEncampments = new MapLocation[allEncampments.length];
		int neInd = 0;

		// Compute nearest encampment (counting the enemy HQ)
		outer: for (MapLocation encamp : allEncampments) {
			for (MapLocation alliedEncamp : alliedEncampments)
				if (alliedEncamp.equals(encamp))
					continue outer;

			// add to neutral encampments list
			neutralEncampments[neInd] = encamp;
			neInd = neInd + 1;

		}
		rc.setIndicatorString(2,
				"neutral enc det " + neInd + " round " + Clock.getRoundNum());

		return neutralEncampments;
	}

	/**
	 * NEAREST ENCAMP<br>
	 * Finds the closest encampment to the HQ.
	 * 
	 * @param encampments
	 *            An array of encampments.
	 * @param searchPoint
	 *            the MapLocation we are searching from.
	 * @return MapLocation; The closest encampment to the HQ.
	 * @throws GameActionException
	 */
	public static MapLocation nearestEncamp(MapLocation[] encampments,
			MapLocation searchPoint) throws GameActionException {
		// locate uncaptured encampments within a certain radius
		MapLocation closestFreeEncamp = null;
		int shortestDist = 100000000;
		int dist;

		// Compute nearest encampment (counting the enemy HQ)
		for (MapLocation encamp : encampments) {
			if (encamp != null) {
				dist = searchPoint.distanceSquaredTo(encamp);
				if (dist < shortestDist) {
					shortestDist = dist;
					closestFreeEncamp = encamp;
				}
			}

		}

		if (shortestDist < 10000000) {
			// proceed to an encampment and capture it
			return closestFreeEncamp;
		} else {// no encampments to capture; change state
			return null;
		}
	}

	/**
	 * FAR FROM ENEMY ENCAMP<br>
	 * Finds encampment farthest from enemy HQ.
	 * 
	 * @param encampments
	 *            An array of encampments.
	 * @return MapLocation; The farthest from the enemy HQ.
	 * @throws GameActionException
	 */
	public static MapLocation farFromEnemyEncamp(MapLocation[] encampments)
			throws GameActionException {
		// locate uncaptured encampments within a certain radius
		MapLocation farthestEncamp = null;
		int longestDistance = -1;
		int dist;

		// Compute nearest encampment (counting the enemy HQ)
		for (MapLocation encamp : encampments) {
			if (encamp != null) {
				dist = enemyHQLoc.distanceSquaredTo(encamp);
				if (dist > longestDistance) {
					longestDistance = dist;
					farthestEncamp = encamp;
				}
			}
		}
		if (longestDistance > -1) {
			// proceed to an encampment and capture it
			return farthestEncamp;
		} else {// no encampments to capture; change state
			return null;
		}
	}
}