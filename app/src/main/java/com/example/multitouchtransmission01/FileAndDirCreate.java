package com.example.multitouchtransmission01;

import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class FileAndDirCreate { //創造傳輸用的目錄與資料夾
	
	File sdDir = null; String saveSD = ""; //紀錄 sdcard 路徑
	File rootDir = null; String saveROM = ""; //紀錄 rom 內的路徑
	boolean[] getState = new boolean[2];//0:root, 1:sdcard
	public String getPath = "";
	
	FileAndDirCreate() { }
	
	public boolean sdcardDetect() { return (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)); }
	
	public boolean checkFile(String filePath) {
		File newFile = new File(filePath);
		if(newFile.exists()) { return true; } else { return false; }
	}
	
	public int createDict() {
		if(sdcardDetect()) { //有sdcard，創資料夾於 sdcard
			getState[1] = true; getState[0] = false;
			sdDir = Environment.getExternalStorageDirectory();//取得 sdcard 目錄
			saveSD = sdDir.getPath() + "/MTImage/";
			getPath = saveSD;
			if(! checkFile(saveSD)) { File newFile = new File(saveSD); newFile.mkdir(); } //沒有就創建一個資料夾
			return 1;
		}
		else { //沒有 sdcard，創建料夾 rom
			getState[1] = false; getState[0] = true;
			rootDir = Environment.getRootDirectory();
			saveROM = rootDir.getPath() + "/MTImage/";
			getPath = saveROM;
			if(! checkFile(saveROM)) { File newFile = new File(saveROM); newFile.mkdir(); } //沒有就創建一個資料夾
			return 1;
		}
	}
	
	public void createFile() {
		String getFilePath = "";
		for(int i = 0 ; i < 24 ; i++) {
			getFilePath = getPath + "//" + (i) + ".webp";
			if(! checkFile(getFilePath)) {
				File newFile = new File(getFilePath);
				try { newFile.createNewFile(); } catch (IOException e) {  }
			}
		}
	}
}
