package com.example.multitouchtransmission01;

public class TransferInfomation {
	
	public double ARMTouchX = 1920.0;
	public double ARMTouchY = 1080.0;
	
	TransferInfomation() {
		
	}
	
	//轉換點座標
	public int switchPointXorY(int getoption, int value, double getScaleX, double getScaleY) {
		int div = 0;
		double left = 0.0;
		int int_left = 0;
		switch(getoption) {
			case 0: //x
				div = (int) getScaleX;
				left = ((double) value) * (getScaleX - div);
				int_left = (int) left;
				return (div*value + int_left);
			case 1: //y
				div = (int) getScaleY;
				left = ((double) value) * (getScaleY - div);
				int_left = (int) left;
				return (div*value + int_left);
		}
		return (div + int_left);
	}
	
	public String pingForFindAndCheck() {
		String strForFindAndCheck = "<?xml version=\"1.0\"?> <ConnectInfo xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <ConnectMode>Ping</ConnectMode> </ConnectInfo>";
		return strForFindAndCheck;
	}
	
	public String getMessage(String getMsgStr, String deviceName, String deviceID) { //傳訊息
		String transferMessage = "<?xml version=\"1.0\"?> <ConnectInfo xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <ConnectMode>Message</ConnectMode>" +
				" <Message>" + getMsgStr + "</Message>" +
				"<ComputerName>"+ deviceName +"</ComputerName> <UserName>"+ deviceName +"</UserName> " +
				"<ConnectHash>"+ deviceID +"</ConnectHash> " + " </ConnectInfo>";
		return transferMessage;
	}
	
	public String getPoints(int getPointCount, DeviceInfoAndUser diau, TransferPointsInfomation getTPI, ImageTransmission getImtrs) { //傳點
		String transferPoint = "<ConnectInfo xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> " +
				"<ComputerName>" + diau.deviceName + "</ComputerName> <UserName>" + diau.deviceName + "</UserName> " +
				"<ConnectHash>" + diau.deviceID + "</ConnectHash> ";
		switch(getPointCount) {
			 case 1:
				 transferPoint = transferPoint +
				"<ConnectMode>Touch</ConnectMode> <Action1>Down</Action1><Action2>Down</Action2><Action3>Down</Action3><Action4>Down</Action4>" +
				"<Point1> <X>"+ Integer.toString(switchPointXorY(0,getTPI.eachPointXY[0][0],ARMTouchX/diau.getDeviceX,ARMTouchY/diau.getDeviceY)) +"</X><Y>" 
				+ Integer.toString(switchPointXorY(1,getTPI.eachPointXY[0][1],ARMTouchX/diau.getDeviceX,ARMTouchY/diau.getDeviceY)) + "</Y> " +
				"</Point1> " + "<Point2> <X>0</X><Y>0</Y> </Point2> " + "<Point3> <X>0</X><Y>0</Y> </Point3> " + "<Point4> <X>0</X><Y>0</Y> </Point4> " + "<PointC>1</PointC> ";
				break;
			 case 2:
				 transferPoint = transferPoint +
				"<ConnectMode>Touch</ConnectMode> <Action1>Down</Action1><Action2>Down</Action2><Action3>Down</Action3><Action4>Down</Action4>" +
				"<Point1> <X>"+ Integer.toString(switchPointXorY(0,getTPI.eachPointXY[0][0],ARMTouchX/diau.getDeviceX,ARMTouchY/diau.getDeviceY)) +"</X><Y>" 
				+ Integer.toString(switchPointXorY(1,getTPI.eachPointXY[0][1],ARMTouchX/diau.getDeviceX,ARMTouchY/diau.getDeviceY)) + "</Y> " +
				"</Point1> " + "<Point2> <X>"+ Integer.toString(switchPointXorY(0,getTPI.eachPointXY[1][0],ARMTouchX/diau.getDeviceX,ARMTouchY/diau.getDeviceY)) +"</X><Y>" 
				+ Integer.toString(switchPointXorY(1,getTPI.eachPointXY[1][1],ARMTouchX/diau.getDeviceX,ARMTouchY/diau.getDeviceY)) + "</Y> " +
				"</Point2> " + "<Point3> <X>0</X><Y>0</Y> </Point3> " + "<Point4> <X>0</X><Y>0</Y> </Point4> " + "<PointC>2</PointC> ";
				 break;
		}
		transferPoint = transferPoint + " </ConnectInfo>";
		return transferPoint;
	}
	
	public String getScreen(String deviceName, String deviceID) {
		String getScreenStr = "<?xml version=\"1.0\"?> <ConnectInfo xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
				"<ComputerName>" + deviceName + "</ComputerName> <UserName>" + deviceName + "</UserName> " +
				"<ConnectHash>" + deviceID + "</ConnectHash> " + 
				" <ConnectMode>Screen</ConnectMode> " + 
				" </ConnectInfo>";
		return getScreenStr;
	}
	
}
