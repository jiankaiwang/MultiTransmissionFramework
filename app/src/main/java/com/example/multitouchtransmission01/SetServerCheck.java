package com.example.multitouchtransmission01;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

public class SetServerCheck {
	public int checkServer; //可以用來確認是否有此伺服器
	TransferInfomation tI;
	public String serverIPForCheck = "";
	byte[] sendData = new byte[2048]; //傳輸內容，共同使用的緩衝區
	
	@SuppressLint({ "NewApi", "NewApi" })
	SetServerCheck(String getServer) {
		if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
		checkServer = 0;
		serverIPForCheck = getServer;
		initialFun();
	}
	
	public void initialFun() {
		tI = new TransferInfomation();
		getCheck();
	}
	
	public void getCheck() {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(serverIPForCheck);
			String sentence_start = tI.pingForFindAndCheck();
			sendData = sentence_start.getBytes();
			DatagramPacket sendPacket_start = new DatagramPacket(sendData, sendData.length, IPAddress, 23152);
			//宣告準備傳送出去的 packet，並說明傳輸內容名稱(sendData)、長度(.length)及 server 的 port
			clientSocket.send(sendPacket_start);//傳送封包，將包含資料、長度及 port 等訊息，傳送出去
			
			DatagramPacket receivePacket = new DatagramPacket(sendData,sendData.length);
			clientSocket.setSoTimeout(2000);
			try { clientSocket.receive(receivePacket); }
			catch (java.net.SocketTimeoutException ste) { checkServer = 0; return ; }
			checkServer = 1;
			
		} catch (SocketException e) {} 
		  catch (UnknownHostException e) {} 
		  catch (IOException e) {}
	}
}
