package ObjectivlyMoreAwesome;

import battlecode.common.Direction;

/**
 * 
 * @author Logan Esch, Andrew Wilson, Michael Manning
 * @category Code for HQ.
 * 
 */
public class HQ extends RobotPlayer {

	/**
	 * The number of units we have spawned. Used to determine soldier type.
	 */
	static int unitsSpawned = 0;

	/**
	 * RUN<br>
	 * Run HQ code.
	 */
	public static void run() {

		while (true) {
			try {
				/**
				 * Initialize locations.
				 */
				if (hqLoc == null) {
					hqLoc = rc.senseHQLocation();
				}
				if (enemyHQLoc == null) {
					enemyHQLoc = rc.senseEnemyHQLocation();
				}

				if (rc.isActive()) {

					/**
					 * Get upgrades if we don't have them yet.
					 * TODO: Are there upgrades? 
					 */

					/**
					 * Spawn a Soldier.
					 */
					 if (rc.isActive() && rc.senseRobotCount() < 25) {
						// 0: straight, 1: Right 45', -1: Left 45', 2:Right 90',
						// -2:Left 90'

						int[] directionOffsets = { 0, 1, -1, 2, -2 };

						lookAround: for (Direction d : Direction.values()) {
							Direction lookingAtCurrently = null;
							for (int di : directionOffsets) {
								// Doesn't this make d=(0 through 4)?
								lookingAtCurrently = Direction.values()[(d
										.ordinal() + di + 8) % 8];

								if (rc.canMove(lookingAtCurrently)) {
									rc.spawn(lookingAtCurrently);
									unitsSpawned++;
									break lookAround;
								}
							}
						}
						/*
						 * TODO: Optimize upgrades. which ones we want, when we
						 * want them. which map sizes do we want them on? right
						 * now we only research if we are out of energy. else if
						 * (!rc.hasUpgrade(Upgrade.PICKAXE)) {
						 * rc.researchUpgrade(Upgrade.PICKAXE); }
						 */
						/**
						 * Get other upgrades if we are out of energy.
						 */
					}  

					/**
					 * Broadcast how many units have spawned. <br>
					 * Used to determine
					 * soldier types.
					 * 
					 * TODO Should we only update if info changes? what about
					 * bad info?
					 */
					int channel = getChannel();
					rc.broadcast(channel + 2, unitsSpawned);
				}

			} catch (Exception e) {
				System.out.println("HQ Exception");
				e.printStackTrace();
			}
			rc.yield();
		}

	}

}