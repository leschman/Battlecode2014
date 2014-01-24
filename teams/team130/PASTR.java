package team130;

import battlecode.common.GameActionException;

public class PASTR extends RobotPlayer{
	
	private static boolean locationRegistered = false; 
	
	public static void run(){
		try{
			myLoc = rc.getLocation();
			int channel = checkLocationRegistered();
			if(!locationRegistered){

				rc.broadcast(channel, mapLocationToInt(myLoc));
			}
		}catch(Exception e){
			System.out.println("PASTR exception");
			e.printStackTrace();
		}
	}
	
	private static int checkLocationRegistered() throws GameActionException{
		//set false while we check. 
		locationRegistered = false;
		int myLocInt = mapLocationToInt(myLoc);
		int lastOpenChannel = -1;
		
		for(int i = 20; i < 40; i++){
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
