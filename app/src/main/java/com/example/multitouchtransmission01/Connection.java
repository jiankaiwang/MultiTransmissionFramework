package com.example.multitouchtransmission01;

/* 檔名：Connection.java
 * 裝置：Android
 * (1) 為單一類別，可以獨立使用
 * (2) 經創建物件，可回傳給對話視窗等使用
 * 概念：針對內網 IP 的位置，如 192.168.1.24，對 3 個不同網域(.0.255 / .1.255 / .2.255)進行廣播，並預設每個網域有 5 個主機。
 * 連線後，利用 thread 進行接收回傳封包， 若是有回傳，就存入廣域的字串陣列，為避免不同的資源同時被使用，而有覆蓋問題，利用 index 來儲存相對
 * 的字串位置。若是產生的第二個 thread 有接收到回傳封包，就將回傳封包的位置存入字串第 2 的位置。要選擇時，僅需要將字串陣列進行搜尋便可以知道
 * 有哪些可能可以使用的主機。若是可用的主機位置不是在這些網域中，則可用手動輸入來進行處理。
 * */

import android.annotation.SuppressLint;
import android.os.StrictMode;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

class Connection {
	//資料傳輸與紀錄
	byte[] sendData = new byte[2048]; //共同使用的緩衝區
	public String[] getServerIP = new String[15]; //紀錄每個 server 位置的陣列
	public int countServer = 0; //紀錄有多少的 server
	//紀錄連線伺服器的基本資料
	public String usingIP = "", serIPSite = "", totalSerIP = ""; //內網 ip、紀錄 serverIP 位置
	private int serverPort = 23152; //並記錄 Port number
	//設置 ARMTouch 長寬，為固定值，與當時 Server 的解析度無關
	final double ARMTouchX = 1920.0, ARMTouchY = 1080.0;
	//取得放大比例
	double getScaleX = 0, getScaleY = 0;
	
	@SuppressLint({ "NewApi", "NewApi" })
	public Connection() {
		//google SDK > 9 時需要先將允許限制，若是單純設定 AndroidManifest.xml 內的 permission 仍是不足
		if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
		//每次都需要先將儲存的陣列初始化
		for(int i = 0 ; i < 15 ; i++) { getServerIP[i] = ""; }
		try { getServerIP(); } catch (UnknownHostException e) { }
		sendAndReceive();
		saveChoice();
	}
	
	class findThread extends Thread {
		byte[] receiveData = new byte[2048]; //傳輸內容，共同使用的緩衝區
		DatagramSocket serverSocket= null;
		int saveIndex = 0;
		
		public findThread(DatagramSocket getclientSocket, int j, int i) {
			serverSocket = getclientSocket;
			saveIndex = j*5 + i*1;
		}
		public void run() {
			try {
				DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
				serverSocket.receive(receivePacket);
				InetAddress clientIPAddress = receivePacket.getAddress();
				getServerIP[saveIndex] = clientIPAddress.getHostAddress();
			} catch (SocketException e) {
			} catch (IOException e) {
			}	
		}
	}
	
	public void getServerIP() throws UnknownHostException {
		//取得內網 IP 位置，藉由網卡的設定來取得
		try {  
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {  
                NetworkInterface intf = en.nextElement();  
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {  
                    InetAddress inetAddress = enumIpAddr.nextElement();  
                    if (!inetAddress.isLoopbackAddress()) { usingIP = inetAddress.getHostAddress().toString(); }
                }  
            }  
        } catch (SocketException ex) { }
	}
	
	public void sendAndReceive() {
		//針對取得的內網 IP 進行字串處理，並取得要傳送廣播封包的位置
		String[] vElement = usingIP.split("\\.");
		String IP = vElement[0] + "." + vElement[1] + "." + vElement[2] + ".";
		String TestIP = "";
		for(int j = 0 ; j < 3; j++) {
			int addInt = Integer.parseInt(vElement[2]);
			
			switch (j) {
				case 0: TestIP = vElement[0] + "." + vElement[1] + "." + Integer.toString(addInt) + ".255";  break; // 後面加上255
				case 1: if(addInt > 0) { TestIP = vElement[0] + "." + vElement[1] + "." + Integer.toString(addInt-1) + ".255"; } else { continue; } break;
				case 2:	TestIP = vElement[0] + "." + vElement[1] + "." + Integer.toString(addInt+1) + ".255"; break;
			}
			
			try {
				DatagramSocket clientSocket = new DatagramSocket();
				InetAddress IPAddress = InetAddress.getByName(TestIP);
				
				String sentence_start = "<?xml version=\"1.0\"?> <ConnectInfo xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <ConnectMode>Ping</ConnectMode> </ConnectInfo>";
				
				sendData = sentence_start.getBytes();
				DatagramPacket sendPacket_start = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
				clientSocket.send(sendPacket_start);//傳送封包，將包含資料、長度及 port 等訊息，傳送出去
				
				for(int i = 0; i < 5; i++) {
					findThread findIP = new findThread(clientSocket, j, i);
					findIP.start();
					if(i == 4) { try { findIP.join(2000); } catch (InterruptedException e) { } } //.join() 設定等待 thread 的時間，超過後就開始進行後續處理
				}	
			} catch (SocketException e) {} 
			  catch (UnknownHostException e) {} 
			  catch (IOException e) {}
		}
	}
	
	public void saveChoice() {
		String strTemp = "";
		int firstFlag = 0;
		for(int i = 0; i < 3 ; i++) {
			for(int j = 0; j < 5; j++) {
				strTemp = getServerIP[i*5 + j*1];
				if(strTemp != "") {
					countServer += 1;
					totalSerIP = totalSerIP + "|" + strTemp; 
					if(firstFlag == 0) { serIPSite = strTemp; firstFlag = 1; }
				}
			}
		}
	}
}
