package team130;

import battlecode.common.Clock;
import battlecode.common.MapLocation;

/**
 * 
 * @author Logan Esch, Andrew Wilson, Micheal Manning
 * @category Code for offensive and defensive soldiers.
 * 
 */
public class Marine extends Soldier {

	/**
	 * RUN<br>
	 * 
	 * Run Marine Code.
	 */
	public static void run() {
		try {
			/**
			 * Declare us to be a Marine for debugging
			 */
			rc.setIndicatorString(0, "Marine");
			/**
			 * Creates a wave of mines to advance on the enemy
			 */
			// mineDefenceStrategy();

			if (rallyPt == hqLoc) {
				int msg = rc.readBroadcast(getChannel());
				if (msg != 0) {
					rc.setIndicatorString(1, "Recieved: " + msg);
					rallyPt = intToMaplocation(msg);
				}

			}

			/**
			 * Determines when we should attack based on round number.
			 */
			MapLocation goal = rallyPt;
			int clock = Clock.getRoundNum();
			if (clock < 250) {
				goal = rallyPt;
			} else if (clock < 550) {
				goal = enemyHQLoc;
			} else if (clock < 700) {
				goal = rallyPt;
			} else if (clock < 900) {
				goal = enemyHQLoc;
			} else if (clock < 1300) {
				goal = rallyPt;
			} else if (clock < 1600) {
				goal = enemyHQLoc;
			} else if (clock < 2000) {
				goal = rallyPt;
			} else {
				goal = enemyHQLoc;
			}
			status = 1;
			/**
			 * TODO: We need to explore not using mines to attack, or in
			 * emergency. or to capture encampments
			 */
			if (status == 1) {// don't lay mines
				// move toward received goal, using swarm behavior
				if (!(goal == rallyPt && myLoc.isAdjacentTo(goal))) {
					swarmMove(goal);
				}
				// lay mines in a checkerboard pattern if status == 2
				// and there are no nearby enemies
			} else { // DON'T EVER DELETE THIS OR YOU WILL DIE!!!!
				// if not laying mines, (enemy close) use swarm behavior
				swarmMove(goal);
			}
		} catch (Exception e) {
			System.out.println("Marine Exception");
			e.printStackTrace();
		}

	}

	

	

}