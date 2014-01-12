package ObjectivlyMoreAwesome;

import battlecode.common.*;

/**
 * 
 * @author Logan Esch, Andrew Wilson, Michael Manning
 * @category Base class for our Robot Player.
 * 
 */
public class RobotPlayer {

	/**
	 * The robot controller.
	 */
	static RobotController rc;

	/**
	 * Seed for messaging channel randomization.
	 */
	static int mult = 271;

	/**
	 * Current location of robot.
	 */
	static MapLocation myLoc;

	/**
	 * Friendly HQ location.
	 */
	static MapLocation hqLoc;

	/**
	 * Enemy HQ Location.
	 */
	static MapLocation enemyHQLoc;
	
	/**
	 * The channel we are communicating on. 
	 */
	static int channel;

	/**
	 * RUN <br>
	 * 
	 * Main function to determine type of robot and run that type's code.
	 * 
	 * @param myRC
	 *            Robot controller for calling functions.
	 */
	public static void run(RobotController myRC) {
		rc = myRC;
		if (rc.getTeam() == Team.A)
			/**
			 * Change mult base on what team we are.
			 */
			mult = 104;

		while (true) {
			try {
				/**
				 * Select what type of robot is executing and run it's code.
				 */
				if (rc.getType() == RobotType.SOLDIER) {
					Soldier.run();
				} else if (rc.getType() == RobotType.HQ) {
					HQ.run();
				}
			} catch (Exception e) {
				System.out.println("caught exception before it killed us:");
				e.printStackTrace();
			}
			rc.yield();
		}
	}

	/**
	 * GET CHANNEL<br>
	 * 
	 * Scrambling method to scramble the channel.
	 * 
	 * @return int; The channel we are using this round.
	 */
	public static int getChannel() {
		int clock = Clock.getRoundNum();
		int channel = (clock * mult + 6) % GameConstants.BROADCAST_MAX_CHANNELS;
		return channel;
	}

	/**
	 * MAP LOCATION TO INT<br>
	 * 
	 * Converts a mapLocation into an int for sending messages.
	 * 
	 * @param loc
	 *            MapLocation to broadcast.
	 * @return int; Representing that location
	 * 
	 *         TODO: get rid of scrambling, not needed in this version. 
	 */
	public static int MapLocationToInt(MapLocation loc) {
		return loc.x * 1000 + loc.y;
	}

	/**
	 * INT TO MAP LOCATION<br>
	 * 
	 * Converts an int into a mapLocation.
	 * 
	 * @param mint
	 *            The int from the message.
	 * @return MapLocation; From the message int.
	 * 
	 *         TODO: get rid of scrambling, not needed in this version. 
	 */
	public static MapLocation intToMaplocation(int mint) {
		int y = mint % 1000;
		int x = (mint - y) / 1000;
		if (x == 0 && y == 0) {
			return null;
		} else {
			return new MapLocation(x, y);
		}
	}

}