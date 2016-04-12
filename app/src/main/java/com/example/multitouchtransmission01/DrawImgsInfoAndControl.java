package com.example.multitouchtransmission01;

public class DrawImgsInfoAndControl {
	//控制畫的張數為何
	public int[] changeBufferForDrawing;
	public int[] senderRateIs1 = {1,0};
	public int[] senderRateIs2 = {4,0,1,2,3};
	public int[] senderRateIs3 = {9,0,1,2,3,4,5,6,7,8};
	public int[] senderRateIs4 = {12,0,1,2,3,4,5,6,7,8,9,10,11};
	public int[] senderRateIs5 = {15,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14};
	public int[] senderRateIs6 = {15,0,1,2,3,4,6,7,8,9,10,12,13,14,15,16};
	public int[] senderRateIs7 = {15,0,1,2,3,4,7,8,9,10,11,14,15,16,17,18};
	public int[] senderRateIs8 = {15,0,1,2,3,4,8,9,10,11,12,16,17,18,19,20};
	public int[] senderRateIs9 = {15,0,1,2,3,4,9,10,11,12,13,18,19,20,21,22};
	public int drawCount; //紀錄有幾張要畫
	public int[] stopImgIndex = {1,4,9,12,15,17,19,21,23};
	public int[][] needToDrawIndex = new int[9][23];
	public int[][] flagToDraw = new int[9][23]; //若是一直沒有畫，則會再繪圖，可以處理再連線網路頻寬較差的繪圖效果
	
	DrawImgsInfoAndControl() { 
		initialFun();
	}
	
	public void initialFun() {
		drawCount = 0;
		totalToClean();
	}
	
	public void totalToClean() { //清除所有應該要更新的旗標
		for(int i = 0 ; i < 9 ; i++) {
			for(int j = 0 ; j < 23 ; j++) {
				needToDrawIndex[i][j] = flagToDraw[i][j] = 0;
			}
		}
	}
}
