package com.example.multitouchtransmission01;

public class TransferPointsInfomation {
	public int pointCount = 0; //有幾個點
	public int[][] eachPointXY = new int[10][2]; //紀錄每個點的  X, Y 座標
	
	TransferPointsInfomation() {
		initialFun();
	}
	
	private void initialFun() {
		for(int i = 0 ; i < 10 ; i++) {
			for(int j = 0 ; j < 2 ; j++) { eachPointXY[i][j] = 0; }
		}
		pointCount = 0;
	}
	
}
