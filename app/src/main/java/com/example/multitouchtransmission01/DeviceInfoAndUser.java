package com.example.multitouchtransmission01;

import java.util.Random;

public class DeviceInfoAndUser {
	//裝置內容
	public String deviceName = ""; //裝置名稱
	public String deviceID = ""; //裝置 ID
	public int getDeviceX = 0; //裝置寬度
	public int getDeviceY = 0; //裝置高度
	
	DeviceInfoAndUser() {
		deviceName = "device01";
		deviceID = getAcess();
		getDeviceX = 480;
		getDeviceY = 960;
	}
	
	public String getAcess(){
		Random rnd = new Random();
		String rndNum = "";
		int number = 0;
		for(int i = 0; i < 9; i++) {
			number = rnd.nextInt(8)+1;
			rndNum += Integer.toString(number);
		}
		return rndNum;
	}
}
