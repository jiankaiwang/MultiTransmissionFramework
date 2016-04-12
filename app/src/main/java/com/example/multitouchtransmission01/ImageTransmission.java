package com.example.multitouchtransmission01;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class ImageTransmission extends Thread {

	private Handler handler = new Handler();
	static int initialWidth = 0, initialHeight = 0; 
	static int nonDevWidth = 0, nonDevHeight = 0; //未切寬、高
	static int devWidth = 0, DevHeight = 0; //已切寬、高
	static int[] recordMap = new int[81]; //用一維來記錄圖片狀態， 0 沒有圖，1 有圖，2 有更新(僅繪圖部分)，3 正在讀取
		static int[][] recordMapXY = new int[81][2]; //用二維來紀錄 x, y 座標
	String getPath = ""; //亦可給 timer 用
	DatagramSocket clientSocketGetImage ; //亦可以給 timer 使用
	File sdDir = null; String saveSD = ""; //紀錄 sdcard 路徑
	File rootDir = null; String saveROM = ""; //紀錄 rom 內的路徑
	boolean[] getState = new boolean[2];//0:root, 1:sdcard
	static double ARMTouchImgX, ARMTouchImgY; //設置 ARMTouch 長寬
	static double getImgScaleX, getImgScaleY; //取得放大比例
	DeviceInfoAndUser diau; //裝置資訊
	ServerInfoAndCheck siac; //伺服器資訊
	TransferInfomation tI = new TransferInfomation();//傳送 xml
	byte[] dataTransmission = new byte[20480]; //傳輸內容，共同使用的緩衝區	
	DrawImgsInfoAndControl diic;
	
	public int createDict() {
		if(sdcardDetect()) { //有sdcard，創資料夾於 sdcard
			getState[1] = true; getState[0] = false;
			sdDir = Environment.getExternalStorageDirectory();//取得 sdcard 目錄
			saveSD = sdDir.getPath() + "/MTImage/";
			if(! checkFile(saveSD)) { File newFile = new File(saveSD); newFile.mkdir(); } //沒有就創建一個資料夾
			return 1;
		}
		else { //沒有 sdcard，創建料夾 rom
			getState[1] = false; getState[0] = true;
			rootDir = Environment.getRootDirectory();
			saveROM = rootDir.getPath() + "/MTImage/";
			if(! checkFile(saveROM)) { File newFile = new File(saveROM); newFile.mkdir(); } //沒有就創建一個資料夾
			return 1;
		}
	}
	
    ImageTransmission(DeviceInfoAndUser getDiau, ServerInfoAndCheck getSiac, DrawImgsInfoAndControl getDiic) {
    	int getCreateDir = createDict(); //創建資料夾，用 getCreateDir 查看創建資料夾狀況
    		if(getCreateDir != 1) { return ; }
		diau = getDiau;
		siac = getSiac;
		diic = getDiic;
    	getStartImg();
    }
    
    public boolean sdcardDetect() { return (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)); }
	
	public boolean checkFile(String filePath) {
		File newFile = new File(filePath);
		if(newFile.exists()) { return true; } else { return false; }
	}
    
    public void getNeedInfo(String getInfo) {
		//字串處理，取出必要的內容
		String[] vElement = getInfo.split("\\|");
		String dealStr = "|" + vElement[0] + "|" + vElement[1] + "|" + vElement[2] + "|"
						+ vElement[3] + "|" + vElement[4] + "|" + vElement[5] + "|" + vElement[6] + "|";
		initialWidth = Integer.parseInt(vElement[1]); initialHeight = Integer.parseInt(vElement[2]);
		nonDevWidth = Integer.parseInt(vElement[3]); ARMTouchImgX = (double)nonDevWidth;
		nonDevHeight = Integer.parseInt(vElement[4]); ARMTouchImgY = (double)nonDevHeight;
		devWidth = Integer.parseInt(vElement[5]); DevHeight = Integer.parseInt(vElement[6]);
		//現有螢幕尺寸 / 取得畫面  = 縮放比例
		getImgScaleX = diau.getDeviceX / ARMTouchImgX; getImgScaleY = diau.getDeviceY / ARMTouchImgY;
	}
    
    public void getStartImg() {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(siac.serIPSite);
			//-----------------------------------------傳送端------------------------------------------------
			String sentence_start = tI.getScreen(diau.deviceName, diau.deviceID);
			dataTransmission = sentence_start.getBytes();
			DatagramPacket sendPacket_start = new DatagramPacket(dataTransmission, dataTransmission.length, IPAddress, siac.serverPort);
			//宣告準備傳送出去的 packet，並說明傳輸內容名稱(sendData)、長度(.length)及 server 的 port
			clientSocket.send(sendPacket_start);//傳送封包，將包含資料、長度及 port 等訊息，傳送出去
				//clientSocket.close(); 會造成錯誤連線
			//-----------------------------------------接收端------------------------------------------------
			clientSocketGetImage = new DatagramSocket(23160);
			if(getState[0]) { getPath = saveROM; } //rom
				else if(getState[1]) { getPath = saveSD; } //sd
			
			DatagramPacket receivePacket = new DatagramPacket(dataTransmission,dataTransmission.length);
			//接收資料，並宣告 receivePacket 作為接收的對象，內含資料及長度，因 port 為程序確認用，若資料近來代表 port 正確，之後可不須再將 port 讀入
			clientSocketGetImage.setSoTimeout(100);
			try { clientSocketGetImage.receive(receivePacket);//接收封包
			} catch (java.net.SocketTimeoutException ste) { return ; }
			
			String getScreenData = new String(receivePacket.getData(), 0, receivePacket.getLength());
			getNeedInfo(getScreenData); //處理畫面大小等狀況
			
	    	handler.postAtFrontOfQueue(getImg);
			//handler.postDelayed(getImg, 0);
			
		} catch (SocketException e) {
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
    }

    public void onDestroy() { handler.removeCallbacks(getImg); }
    
    private Runnable getImg = new Runnable() {
        public void run() {
        	handler.postDelayed(getImg, 20);
            getImageDetail getI1 = new getImageDetail(); getI1.run();
        }
    };
    
    class getImageDetail extends Thread {
    	byte[] ImgTransmission = new byte[10240]; //不可共用 dataTransmission，否則會出錯，因為每個 thread 都在用
    	getImageDetail() { }
    	public void run() {
    		try {
    			DatagramPacket receivePacket = new DatagramPacket(ImgTransmission,ImgTransmission.length);
				//接收資料，並宣告 receivePacket 作為接收的對象，內含資料及長度，因 port 為程序確認用，若資料近來代表 port 正確，之後可不須再將 port 讀入
	    		clientSocketGetImage.setSoTimeout(5);
				try { clientSocketGetImage.receive(receivePacket); //接收封包
				} catch (java.net.SocketTimeoutException ste) { return ; }
				//檢查是否為正確的來源，可以中途換影像
				//if(! (("/"+siac.serIPSite).equals(receivePacket.getAddress().toString())) ) { return ; }
				String getScreenData = new String(receivePacket.getData(), 0, 6);
				if(getScreenData.equals("[INFO]")) {
					getScreenData = new String(receivePacket.getData(), 6, receivePacket.getLength()); getNeedInfo(getScreenData);
					 int count = 0;
				     while(true) {
						getImageData getImgThread = new getImageData(clientSocketGetImage ,siac, nonDevWidth, nonDevHeight, recordMap, getPath, diic);
						getImgThread.run();
						if(getImgThread.stopSingal == 1) { return; } //break; } //已收到 [CLOS]
						if(getImgThread.changeImg == 1) { 
							recordMapXY[getImgThread.indexForTotalImg][0] = getImgThread.imageX; 
							recordMapXY[getImgThread.indexForTotalImg][1] = getImgThread.imageY;
						} //可用於輸出有幾張畫
						else if(count++ > 10) { break; }
					}
				}
				else { //非 [INFO], 多接收
					 int count = 0, firstTime = 0;
					 while(true) {
				    	if(firstTime++ == 0) {
				    		getImageData getImgThread = new getImageData(clientSocketGetImage ,siac, nonDevWidth, nonDevHeight, recordMap, getPath
									, diic,receivePacket,1);
							getImgThread.run();
							if(getImgThread.stopSingal == 1) { return; } //break; } //已收到 [CLOS]
							if(getImgThread.changeImg == 1) { 
								recordMapXY[getImgThread.indexForTotalImg][0] = getImgThread.imageX; 
								recordMapXY[getImgThread.indexForTotalImg][1] = getImgThread.imageY;
							} //可用於輸出有幾張畫
							//continue;
				    	}
				    	getImageData getImgThread = new getImageData(clientSocketGetImage ,siac, nonDevWidth, nonDevHeight, recordMap, getPath, diic);
						getImgThread.run();
						if(getImgThread.stopSingal == 1) { return; } //break; } //已收到 [CLOS]
						if(getImgThread.changeImg == 1) { 
							recordMapXY[getImgThread.indexForTotalImg][0] = getImgThread.imageX; 
							recordMapXY[getImgThread.indexForTotalImg][1] = getImgThread.imageY;
						} //可用於輸出有幾張畫
						else if(count++ > 10) { break; }
					}
				}
    		} catch (SocketException e) {
    		} catch (UnknownHostException e) {
    		} catch (IOException e) {
    		}  
    	}
    }
}
