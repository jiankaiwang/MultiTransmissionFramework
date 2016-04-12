package com.example.multitouchtransmission01;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class getImageData extends Thread {
	
	DatagramSocket getImageSocket = null;
	DatagramSocket sendSocket = null; //傳送 [NEXT] 的 socket
	byte[] imageData = new byte[10240]; //傳輸內容，共同使用的緩衝區
		byte[] leftData = new byte[20];
		byte[] sendData;
	InetAddress IPAddress = null;
	int stopSingal = 0; //檢查是否停止
	int[] recordMAP; //標示哪個畫格要被更新
	int imageX = 0, imageY = 0; //圖片 X 座標, Y 座標
	int nonDevWidth = 0, nonDevHeight = 0;// Server 寬、高
	int changeImg = 0; //標示是否有成功輸出畫面
	int indexForTotalImg = 0;//紀錄要放入資料的 index
	String saveLocation = "";
	ServerInfoAndCheck siac;
	DatagramPacket receivePacketBefore; 
	int switchOnData;
	//--------精確控制影像處理----------
	DrawImgsInfoAndControl diic;
	private int getStopIndex, getWhichToDraw;
	
	getImageData(DatagramSocket getConnectSocket, ServerInfoAndCheck getSiac, int NonDevWidth, int NonDevHeight, int[] getMap, String getLocation, DrawImgsInfoAndControl getDiic) {
		siac = getSiac;
		try { IPAddress = InetAddress.getByName(siac.serIPSite); } 
		catch (UnknownHostException e) {}
		recordMAP = getMap;
		nonDevWidth = NonDevWidth;
		nonDevHeight = NonDevHeight;
		saveLocation = getLocation;
		getImageSocket = getConnectSocket;
		switchOnData = 0;
		diic = getDiic;
		getStopIndex = 0;
		getWhichToDraw = 0;
		getDrawIndex();
	}
	
	getImageData(DatagramSocket getConnectSocket, ServerInfoAndCheck getSiac, int NonDevWidth, int NonDevHeight, 
			int[] getMap, String getLocation, DrawImgsInfoAndControl getDiic, DatagramPacket getPacket , int flag) {
		siac = getSiac;
		try { IPAddress = InetAddress.getByName(siac.serIPSite); } catch (UnknownHostException e) {}
		recordMAP = getMap;
		nonDevWidth = NonDevWidth;
		nonDevHeight = NonDevHeight;
		saveLocation = getLocation;
		receivePacketBefore = getPacket;
		getImageSocket = getConnectSocket;
		switchOnData = flag;
		diic = getDiic;
		getStopIndex = 0;
		getWhichToDraw = 0;
		getDrawIndex();
	}
	
	public static int byteArrayToInt(byte[] b,int offset) {
		int value = 0;
		for(int i = 0 ; i < 4; i++) {
			int shift = (4-1-i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		return value;
	}
	
	public void stopServerSending(int getPort) {
		//---------------------------------------------NEXT---------------------------------------------
		try {
			sendSocket = new DatagramSocket();
			String nextStr = "[NEXT]";
			sendData = nextStr.getBytes();
			DatagramPacket nextPacket = new DatagramPacket(sendData, sendData.length, IPAddress, getPort);
			sendSocket.send(nextPacket);
		} catch (SocketException e1) { }
		catch (IOException e) { }
	}
	
	public int getW() { //是 X 軸第幾張
		return (imageX/200);
	}
	
	public int getH() { //是 Y 軸第幾張
		int locationY = 0;
		if(imageY % 200 == 0) { locationY = imageY/200; }
		else { locationY = (imageY/200) + 1; }
		return locationY;
	}
	
	public void getDrawIndex() {
		//多於畫面不用在寫入檔案，精確控制影像呈現處理
		switch(nonDevWidth) {
			case 200: getStopIndex = diic.stopImgIndex[0]; getWhichToDraw = 0; break;
			case 400: getStopIndex = diic.stopImgIndex[1]; getWhichToDraw = 1; break;
			case 600: getStopIndex = diic.stopImgIndex[2]; getWhichToDraw = 2; break;
			case 800: getStopIndex = diic.stopImgIndex[3]; getWhichToDraw = 3; break;
			case 1000: getStopIndex = diic.stopImgIndex[4]; getWhichToDraw = 4; break;
			case 1200: getStopIndex = diic.stopImgIndex[5]; getWhichToDraw = 5; break;
			case 1400: getStopIndex = diic.stopImgIndex[6]; getWhichToDraw = 6; break;
			case 1600: getStopIndex = diic.stopImgIndex[7]; getWhichToDraw = 7; break;
			case 1800: getStopIndex = diic.stopImgIndex[8]; getWhichToDraw = 8; break;
		}
	}
	
	public void run() {
		try {

			DatagramPacket receivePacket;
			DatagramPacket receivePacket2 = new DatagramPacket(leftData, leftData.length, IPAddress, siac.imageServerPort);

			switch(switchOnData) {
				case 0:
					receivePacket = new DatagramPacket(imageData,imageData.length, IPAddress, siac.imageServerPort);
					getImageSocket.setSoTimeout(4);
					try { getImageSocket.receive(receivePacket); //接收主要資料內容
					} catch (java.net.SocketTimeoutException ste) { return ; }	//return 表示終止此 thread
					String getTestStop = new String(receivePacket.getData(), 0, 6);
						if(getTestStop.equals("[CLOS]")) { stopServerSending(receivePacket.getPort()); stopSingal = 1; return ; }
						else if(getTestStop.equals("[INFO]")) { return ; }
					break;
				default:
					receivePacket = receivePacketBefore;
					String getTestStopDefault = new String(receivePacket.getData(), 0, 6);
						if(getTestStopDefault.equals("[CLOS]")) { stopServerSending(receivePacket.getPort()); stopSingal = 1; return ; }
						else if(getTestStopDefault.equals("[INFO]")) { return ; }
					switchOnData = 0;
					break;
			}
				getImageSocket.setSoTimeout(4);
				try { getImageSocket.receive(receivePacket2); //[PART]
				} catch (java.net.SocketTimeoutException ste) { return ; }

				//位元轉成數字
				byte[] switchByte = new byte[4];
				byte[] getInt = new byte[20]; getInt = receivePacket2.getData();
					for(int i = 0 ; i < 4 ; i++) { switchByte[i] = getInt[receivePacket2.getLength()-i-1]; }
				if(byteArrayToInt(switchByte,0) != receivePacket.getLength()) { return ;} //檢查封包是否出錯，用長度來比較，不用寫入檔案
				
				byte[] getTotal = new byte[10240];	getTotal = receivePacket.getData();
				for(int i = 0 ; i < 4 ; i++) { switchByte[i] = getTotal[3-i]; } //處理 X
					imageX = byteArrayToInt(switchByte,0);
				for(int i = 0 ; i < 4 ; i++) { switchByte[i] = getTotal[7-i]; } //處理 Y
					imageY = byteArrayToInt(switchByte,0);
				indexForTotalImg = getH() * (nonDevWidth/200) + getW(); //取得存入的圖片 index
				
				if(indexForTotalImg >= getStopIndex) { return ; } //其他都可以不用再多於進行檔案處理，因為冗於檔案太多，每個都處理會將低行動裝置效能
				
				if(recordMAP[indexForTotalImg] != 3) { //critical section 處理
					//寫入圖檔，已針對哪一張圖做檔案輸出
					File getOutFile = new File(saveLocation + "//" + (indexForTotalImg) + ".webp"); //要畫的目標
						FileOutputStream fileOutputStream = new FileOutputStream(getOutFile);
					fileOutputStream.write(receivePacket.getData(), 8, receivePacket.getLength());
						fileOutputStream.close();
					changeImg = 1;
					recordMAP[indexForTotalImg] = 2; //有被更新
					diic.needToDrawIndex[getWhichToDraw][indexForTotalImg] = 1; //紀錄更新
				}
		} catch (Exception e) { 
			try { finalize(); } catch (Throwable e1) {} 
		}
	}
}