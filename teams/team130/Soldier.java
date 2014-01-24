package team130;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.TerrainTile;

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

	final static int cowAttraction = 10;

	/**
	 * Attraction from far enemy units when we out-number enemy.
	 */
	final static int offFarEnemyAttraction = 10;

	/**
	 * Variable to control vision used for swarm behavior. <br>
	 * <br>
	 */
	static int vision = 35;
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
						//Defender.run();
						break;
					case PROBE:
						Probe.run();
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
	 * HARD CODED TO ALWAYS MAKE MARINES
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
			type = Type.PROBE;

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
		if(type == Type.PROBE){
			targetWeighting *= 3;
		}

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
		 * Attract to Cows!
		 */
		goalLoc = goalLoc.add(myLoc.directionTo(centerOfHerd()), cowAttraction);

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

	protected static MapLocation centerOfHerd() {
		MapLocation myLoc = rc.getLocation();
		MapLocation[] inVisionRange = MapLocation
				.getAllMapLocationsWithinRadiusSq(myLoc, vision);
		int totalWeight = 1;
		int x = 0;
		int y = 0;
		for (MapLocation loc : inVisionRange) {
			try {
				double weight = rc.senseCowsAtLocation(loc);
				totalWeight += weight;
				x += weight * loc.x;
				y += weight * loc.y;
			} catch (GameActionException e) {
				e.printStackTrace();
				System.out.println("Error trying to find center of herd");
			}
		}
		return new MapLocation(x/totalWeight, y/totalWeight);
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
			if (rc.canMove(lookingAtCurrently)
					&& rc.senseTerrainTile(rc.getLocation().add(
							lookingAtCurrently)) != TerrainTile.VOID) {

				if (!enemiesClose) {
					rc.move(lookingAtCurrently);
					break lookAround;
				}
				// TODO fix this so we avoid mines when enemies are
				// close
				else {
					rc.move(lookingAtCurrently);
					break lookAround;
				}

			}
		}
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

}