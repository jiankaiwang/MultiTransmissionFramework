package com.example.multitouchtransmission01;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MTView extends SurfaceView implements SurfaceHolder.Callback {
	private static final int MAX_TOUCHPOINTS = 10;//假設最多支援 10 個點
	private Paint touchPaints[] = new Paint[MAX_TOUCHPOINTS]; //宣告有幾支畫筆，分別針對不同的點進行標示
	private int colors[] = new int[MAX_TOUCHPOINTS]; //用來標示哪種顏色
	static double getImgScaleX, getImgScaleY;//取得放大比例
	int flag = 0, x = 0, y = 0, x_stop = 0, y_stop = 0; //取得觸碰點使用
	float x_edit = 0, y_edit = 0, x2 = 0, y2 = 0, x3 = 0 ,y3 = 0; //編輯模式使用
	String LocationPath = "";
	byte[] sendData = new byte[204800]; //傳輸內容，共同使用的緩衝區
	ImageTransmission imgTrsn;//取得影像物件
	Bitmap[] bmp = new Bitmap[81]; //繪圖用，最大有 81 張圖 
		Canvas drawImg = null, drawBuffer = null;
	Context getContext;
	DeviceInfoAndUser diau;//裝置資訊用
	ServerInfoAndCheck siac;//主機資訊用
	TransferInfomation tI = new TransferInfomation();
	final CharSequence[] longPressOption = {"觸碰點數目", "傳送訊息", "更新畫面設定", "編輯模式", "螢幕截圖", "重新選擇伺服器","結束"}
			, touchPointsCount = {"1","2"}, refreshOption = {"持續更新畫面","暫時不接收畫面"}
			, editModeOption = {"畫筆顏色選擇","畫筆粗細","清除畫面","空白編輯"}, colorChioce = {"紅色","綠色","藍色","黃色"}
			, paintSizeOption = {"極細","略細","適中(預設)","略粗","最粗"}, searchServerOption = {"自動搜尋新伺服器","手動輸入伺服器"};
	TransferPointsInfomation tPI = new TransferPointsInfomation();
	public int transferPointsCount = 1; //有幾個點進行傳輸
	String newServerSite;//新 Server
	private int choiceFlag = 0; //是否有選擇新 Sever
	//-------換 sender rate 使用，顯示必須要畫的畫面，精確處理畫面呈現部分-------
	DrawImgsInfoAndControl diic; //利用此物件進行處理
	private int getNowDrawIndex;
	private int keepDraw = 2;
	//-------是否更新畫面，或是暫停-------
	public int refreahScreen = 1; //是否換畫面旗標
	int getRecordXY[][]; //紀錄原來座標
	//--------截圖存圖用-----------
	Bitmap vBitmap;
	Canvas vBitmapCanvas;
	//--------編輯模式用-----------
	private int editModeFlag; //每個功能都要注意的旗標，將之關閉
	private static final int MAX_COLOR_CHOICE = 4;//3 種不同顏色
	private Paint touchPaintsForEditing[] = new Paint[MAX_COLOR_CHOICE]; //宣告有幾支畫筆，分別針對不同的點進行標示
	private int chioceColor;
	private int paintSize;
	private int drawPieceCount = 0; //亦必須記錄，因為傳輸影像時會重新計算長寬
	private int countDrawForClean;
	
	@SuppressWarnings("deprecation")
	GestureDetector gestureDetector = new GestureDetector (new MyGestureDetector());
	private class MyGestureDetector extends SimpleOnGestureListener {  
		MyGestureDetector() { }		
        public void onLongPress(MotionEvent e) { 
        	AlertDialog.Builder UnitSelection = new AlertDialog.Builder(getContext);
            UnitSelection.setTitle("功能選單");
            UnitSelection.setSingleChoiceItems(longPressOption, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int itemChoice) {
                    Toast.makeText(getContext.getApplicationContext(), longPressOption[itemChoice], Toast.LENGTH_SHORT).show();
                    switch(itemChoice) {
                		case 0: getPointsCount(); break; //觸碰數目
                		case 1: getTransferFun(); break; //傳送訊息
                		case 2: refreshScreen(); break; //更新畫面
                		case 3: editImgFun(); firstActionForEditing(); break; //編輯模式
                		case 4: captureScreen(); break; //截圖
                		case 5: changeServerAndInputOne(); break; //更改 server
                		case 6: android.os.Process.killProcess(android.os.Process.myPid()); break; //結束
                  } dialog.dismiss(); }});
            AlertDialog alert = UnitSelection.create();
            alert.show(); //創造出選單後，記得要 show() 出來
        	super.onLongPress(e); 
        }  
    } 
	
	public void changeServerAndInputOne() {
		AlertDialog.Builder UnitSelection = new AlertDialog.Builder(getContext);
        UnitSelection.setTitle("選擇搜尋模式");
        UnitSelection.setSingleChoiceItems(searchServerOption, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemChoice) {
                Toast.makeText(getContext.getApplicationContext(), searchServerOption[itemChoice], Toast.LENGTH_SHORT).show();
                switch(itemChoice) {
	        		case 0: changeServer(); break; //自動搜尋
	        		case 1: inputServerIPAndCheck(); break; //手動輸入
                } dialog.dismiss(); 
            }});
        AlertDialog alert = UnitSelection.create();
        alert.show(); //創造出選單後，記得要 show() 出來
	}
	
	public void inputServerIPAndCheck() {
		 AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext);
	     editDialog.setTitle("輸入待確認的伺服器 IP");
	     final EditText eTgetMessage = new EditText(getContext);
	     editDialog.setView(eTgetMessage);
	     editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	  	     public void onClick(DialogInterface arg0, int arg1) {
	  	    	   String getMessageStr = eTgetMessage.getText().toString(); //取得輸入的文字
	  	    	   if(getMessageStr.equals(siac.serIPSite)) { Toast.makeText(getContext, "相同伺服器，請重新輸入", Toast.LENGTH_SHORT).show(); return; } //不用更新 Server
	  	    	   SetServerCheck ssc = new SetServerCheck(getMessageStr);
	  	    	   try { Thread.sleep(5000);
	  	    	   } catch (InterruptedException e) {}
	  	    	   if(ssc.checkServer == 1) { 
	  	    		   siac.serIPSite = getMessageStr; 
	  	    		   Toast.makeText(getContext, getMessageStr, Toast.LENGTH_SHORT).show();
		 			   imgTrsn.siac.serIPSite = getMessageStr; imgTrsn.getStartImg();
		 			   try { Thread.sleep(4000); 
		 			       //不跳出就換 Server 方法 2 → 新物件
		 				   //((Activity) getContext).setContentView(new MTView(getContext,diau,siac));
		 			   } catch (InterruptedException e) { } 
		 			   catch (Throwable e) { }
	  	    	   }
	    	   	   else { Toast.makeText(getContext, "此 Server 不存在", Toast.LENGTH_SHORT).show(); }
	  	     }});
	     editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	  	     public void onClick(DialogInterface arg0, int arg1) {  }});
	     editDialog.show();
	}
	
	public void firstActionForEditing() { //開始編輯
		refreahScreen = 2; getRecordXY(); //停止畫面更新
		editModeFlag = 1;
	}
	
	public void finishEditing() { //結束編輯
		refreahScreen = 1;
		editModeFlag = 0;
	}
	
	public void colorChioceFun() {
		AlertDialog.Builder UnitSelection = new AlertDialog.Builder(getContext);
        UnitSelection.setTitle("畫筆顏色選擇");
        UnitSelection.setSingleChoiceItems(colorChioce, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemChoice) {
                Toast.makeText(getContext.getApplicationContext(), colorChioce[itemChoice], Toast.LENGTH_SHORT).show();
                firstActionForEditing();
                for(int i = 0; i < MAX_COLOR_CHOICE; i++) { 
                	if(itemChoice == i) { chioceColor = i; } 
                	dialog.dismiss(); 
            }}});
        AlertDialog alert = UnitSelection.create();
        alert.show(); //創造出選單後，記得要 show() 出來
	}
	
	public void editImgFun() {
		AlertDialog.Builder UnitSelection = new AlertDialog.Builder(getContext);
        UnitSelection.setTitle("編輯模式選單");
        UnitSelection.setSingleChoiceItems(editModeOption, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemChoice) {
                Toast.makeText(getContext.getApplicationContext(), editModeOption[itemChoice], Toast.LENGTH_SHORT).show();
                firstActionForEditing(); //開始進入編輯模式
                switch(itemChoice) {
	        		case 0: colorChioceFun(); break; //顏色選擇
	        		case 1: paintSizeFun(); break; //畫筆粗細
	        		case 2: clearDraw(); break; //清除畫面
	        		case 3: blankEditing(); break; //空白檔案編輯
                }
                dialog.dismiss(); 
                }});
        AlertDialog alert = UnitSelection.create();
        alert.show(); //創造出選單後，記得要 show() 出來
	}
	
	public void getCheckForbBankEditing() {
		 AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext);
	     editDialog.setTitle("空白檔案初始化");
	     editDialog.setPositiveButton("初始化完畢", new DialogInterface.OnClickListener() {
	  	     public void onClick(DialogInterface arg0, int arg1) { blankEditing(); }});
	     editDialog.show();
	}
	
	public void blankEditing() {
		drawImg = getHolder().lockCanvas(); //SurfaceView 下繪圖必要有
		if(drawImg != null) {
			drawImg.drawColor(Color.WHITE);
			vBitmapCanvas.drawColor(Color.WHITE);
			getHolder().unlockCanvasAndPost(drawImg); //SurfaceView 下繪圖必要有
		}
		drawBuffer = getHolder().lockCanvas(); //double buffer
		if(drawBuffer != null) {
			drawBuffer.drawColor(Color.WHITE);
			getHolder().unlockCanvasAndPost(drawBuffer); //SurfaceView 下繪圖必要有
		}
		if(countDrawForClean++ < 1) { getCheckForbBankEditing(); }
		else { countDrawForClean = 0; }
	}
	
	public void getCheckForClearDraw() {
		 AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext);
	     editDialog.setTitle("清除畫面完畢");
	     editDialog.setPositiveButton("清除完畢", new DialogInterface.OnClickListener() {
	  	     public void onClick(DialogInterface arg0, int arg1) { clearDraw(); }});
	     editDialog.show();
	}
	
	public void clearDraw() {
		drawImg = getHolder().lockCanvas();
		if(drawImg != null) {
			drawImg.drawColor(Color.BLACK);
			vBitmapCanvas.drawColor(Color.BLACK);
	    	for(int i = 0; i < drawPieceCount ; i++) {
	    		drawImg.drawBitmap(bmp[i], (int)(getRecordXY[i][0]*getImgScaleX), (int)(getRecordXY[i][1]*getImgScaleY), null);
	    		vBitmapCanvas.drawBitmap(bmp[i], (int)(getRecordXY[i][0]*getImgScaleX), (int)(getRecordXY[i][1]*getImgScaleY), null);
	    	}
	    	getHolder().unlockCanvasAndPost(drawImg);
		}
		drawBuffer = getHolder().lockCanvas(); //double buffer
		if(drawBuffer != null) {
			drawBuffer.drawBitmap(vBitmap, 0, 0, null);
			getHolder().unlockCanvasAndPost(drawBuffer); //SurfaceView 下繪圖必要有
		}
		if(countDrawForClean++ < 1) { getCheckForClearDraw(); }
		else { countDrawForClean = 0; }
	}
	
	public void paintSizeFun() {
		AlertDialog.Builder UnitSelection = new AlertDialog.Builder(getContext);
        UnitSelection.setTitle("畫筆粗細選擇");
        UnitSelection.setSingleChoiceItems(paintSizeOption, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemChoice) {
                Toast.makeText(getContext.getApplicationContext(), paintSizeOption[itemChoice], Toast.LENGTH_SHORT).show();
                firstActionForEditing(); //開始進入編輯模式
                switch(itemChoice) {
	        		case 0: paintSize = 4; break; case 1: paintSize = 8 ; break; case 2: paintSize = 12; break; 
	        		case 3: paintSize = 16; break; case 4: paintSize = 20; break; 
                }
                dialog.dismiss(); 
                }});
        AlertDialog alert = UnitSelection.create();
        alert.show(); //創造出選單後，記得要 show() 出來
	}
	
	public void captureScreen() {
	    try {
	    	Date date = Calendar.getInstance().getTime();
	    	SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd-hh-mm-ss");
	    	//editModeFlag = 0; //因為畫完可能再畫所以不應該設置成 0
	    	File file = new File(LocationPath + "/" + diau.deviceName + "-" + ft.format(date) + ".png");
	        file.createNewFile();
	        FileOutputStream ostream = new FileOutputStream(file);
	        vBitmap.compress( Bitmap.CompressFormat.PNG, 100, ostream );
	        ostream.close();
	    } catch (Exception e) { }
	}
	
	public void refreshScreen() {
		AlertDialog.Builder UnitSelection = new AlertDialog.Builder(getContext);
        UnitSelection.setTitle("更新畫面選項");
        UnitSelection.setSingleChoiceItems(refreshOption, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemChoice) {
                Toast.makeText(getContext.getApplicationContext(), refreshOption[itemChoice], Toast.LENGTH_SHORT).show();
                for(int i = 0; i < 2; i++) { 
                	if(itemChoice == i) { 
                		refreahScreen = 1 - i; 
                		if(i == 1) { getRecordXY(); } 
                		else if(i == 0) { finishEditing(); } 
                	} dialog.dismiss(); 
                }
            }});
        AlertDialog alert = UnitSelection.create();
        alert.show(); //創造出選單後，記得要 show() 出來
	}
	
	public void getRecordXY() { getRecordXY = imgTrsn.recordMapXY; } //紀錄所有座標位置
	
	@SuppressWarnings("deprecation")
	public void changeServer() {
		finishEditing();
		if(choiceFlag == 0) { getCheck(); }
		try {
			Thread.sleep(2000);
			if(choiceFlag == 1) {
				if(newServerSite.equals(siac.serIPSite)) { return; } //不用更新 Server
				else {
					siac.serIPSite = newServerSite;
					//不跳出就換 Server 方法 1 → 修改接收傳輸的對象
					imgTrsn.siac.serIPSite = newServerSite;
					imgTrsn.getStartImg();
					try {
						Thread.sleep(4000);
						//不跳出就換 Server 方法 2 → 新物件
						//((Activity) getContext).setContentView(new MTView(getContext,diau,siac));
					} catch (InterruptedException e) { } 
					  catch (Throwable e) { }
				}
				choiceFlag = 0;
			}
		} catch (InterruptedException e) { }
	}
	
	public void getCheck() {
	   	Connection reEstbConn = new Connection(); //取得連線資訊
	   	final int reCountServer = reEstbConn.countServer;	   	
	   	if(reCountServer == 0) {
	 	   AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext);
	       editDialog.setTitle("沒有 ARM Server");
	       final TextView eTgetMessage = new TextView(getContext);
	       eTgetMessage.setText("請開啟  ARM Server，並重新搜尋");
	       editDialog.setView(eTgetMessage);
	       editDialog.setPositiveButton("確認", new DialogInterface.OnClickListener() {
	    	   public void onClick(DialogInterface arg0, int arg1) { }
	       });
	       editDialog.show();
	   	}
	   	else {
	 	   String[] reGetServerIP = reEstbConn.getServerIP; 
	 	   final String[] reServerSite = new String[reCountServer];
	 	    	
		   String reStrTemp = ""; int addIndex = 0; 
		   for(int i = 0; i < 3 ; i++) {
			   for(int j = 0; j < 5; j++) { 
				   	    reStrTemp = reGetServerIP[i*5 + j*1];
		      		   	if(reStrTemp != "") { reServerSite[addIndex++] = reStrTemp; }
		      }
		   }
	 	   AlertDialog.Builder UnitSelectionServerRe = new AlertDialog.Builder(getContext);
		   UnitSelectionServerRe.setTitle("選擇 Server");
		   UnitSelectionServerRe.setSingleChoiceItems(reServerSite, 0, new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int itemChoice) {
		            Toast.makeText(getContext.getApplicationContext(), reServerSite[itemChoice], Toast.LENGTH_SHORT).show();
		            for(int i = 0 ; i < reCountServer ; i++) { if(i == itemChoice) { newServerSite = reServerSite[i]; choiceFlag = 1; changeServer(); }}
		            dialog.dismiss(); }});
		    AlertDialog alertServer = UnitSelectionServerRe.create();
		    alertServer.show(); //創造出選單後，記得要 show() 出來
	   	}
    }
	
	public void getPointsCount() { //調整傳輸點數目
		AlertDialog.Builder UnitSelection = new AlertDialog.Builder(getContext);
        UnitSelection.setTitle("觸碰點數目選擇");
        UnitSelection.setSingleChoiceItems(touchPointsCount, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemChoice) {
                Toast.makeText(getContext.getApplicationContext(), touchPointsCount[itemChoice], Toast.LENGTH_SHORT).show();
                for(int i = 0; i < 2; i++) { if(itemChoice == i) { transferPointsCount = i + 1; } finishEditing(); dialog.dismiss(); }}});
        AlertDialog alert = UnitSelection.create();
        alert.show(); //創造出選單後，記得要 show() 出來
	}
	
	public void transferMessage(String getMessageStr) {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(siac.serIPSite);
			String sentence_start = tI.getMessage(getMessageStr, diau.deviceName, diau.deviceID);
			sendData = sentence_start.getBytes();
				DatagramPacket sendPacket_start = new DatagramPacket(sendData, sendData.length, IPAddress, siac.serverPort);
				clientSocket.send(sendPacket_start);//傳送封包，將包含資料、長度及 port 等訊息，傳送出去
		} catch (SocketException e) {
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
	}
	
	void getTransferFun() {
		 AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext);
	     editDialog.setTitle("輸入訊息");
	     final EditText eTgetMessage = new EditText(getContext);
	     editDialog.setView(eTgetMessage);
	     editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	  	     public void onClick(DialogInterface arg0, int arg1) {
	  	    	   finishEditing();
 	  	    	   String getMessageStr = eTgetMessage.getText().toString(); //取得輸入的文字
 	  	    	   transferMessage(getMessageStr);
	  	     }});
	     editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	  	     public void onClick(DialogInterface arg0, int arg1) {  }});
	     editDialog.show();
	}
	
	MTView(Context context, DeviceInfoAndUser getDiau, ServerInfoAndCheck getSiac) {//建構子
		super(context);
		getContext = context;
		SurfaceHolder holder = getHolder(); holder.addCallback(this); 
		setFocusable(true); setFocusableInTouchMode(true);
		diau = getDiau; siac = getSiac;
		diic = new DrawImgsInfoAndControl();
		imgTrsn = new ImageTransmission(diau,siac,diic); //啟動圖片傳送開始
		try { Thread.sleep(4000); init(); } catch (InterruptedException e) {}
		editModeFlag = 0;
	}
	
	private void init() {		
		//繪圖事件
		mHandlerTime.postAtFrontOfQueue(timerRun);
			//mHandlerTime.postDelayed(timerRun, 0);
		drawEachTimeToGet();
		drawPaintDesign(); //畫筆等初始化
		refreahScreen = 1; //預設要一直更換畫面
		vBitmap = Bitmap.createBitmap( diau.getDeviceX, diau.getDeviceY , Bitmap.Config.RGB_565);
		vBitmapCanvas = new Canvas( vBitmap );
		chioceColor = 0; //預設編輯畫筆為紅色
		paintSize = 12; //畫筆粗細預設為 24
		countDrawForClean = 0;
		getNowDrawIndex = 0;
	}
	
	public void drawPaintDesign() {
		//多點下的呈色
		colors[0] = Color.BLUE;	colors[1] = Color.RED; colors[2] = Color.GREEN; colors[3] = Color.YELLOW;
		colors[4] = Color.CYAN; colors[5] = Color.MAGENTA; colors[6] = Color.DKGRAY; colors[7] = Color.WHITE;
		colors[8] = Color.LTGRAY; colors[9] = Color.GRAY;
		//利用畫筆的概念
		for (int i = 0; i < MAX_TOUCHPOINTS; i++) {
			touchPaints[i] = new Paint();
			touchPaints[i].setColor(colors[i]);
		}
		//編輯模式的畫筆
		for (int i = 0; i < MAX_COLOR_CHOICE; i++) {
			touchPaintsForEditing[i] = new Paint();
			switch(i) {
				case 0: touchPaintsForEditing[i].setColor(colors[1]); break; //R
				case 1: touchPaintsForEditing[i].setColor(colors[2]); break; //G
				case 2: touchPaintsForEditing[i].setColor(colors[0]); break; //B
				case 3: touchPaintsForEditing[i].setColor(colors[3]); break; //Y
			}
		}
	}
	
	private void drawEachTimeToGet() {
		//取得必要的影像資訊
		if(imgTrsn.getState[0]) { LocationPath = imgTrsn.saveROM; } //rom
		else if(imgTrsn.getState[1]) { LocationPath = imgTrsn.saveSD; } //sd
		getImgScaleX = diau.getDeviceX / imgTrsn.ARMTouchImgX; 
		getImgScaleY = diau.getDeviceY / imgTrsn.ARMTouchImgY;
		//精確處理影像部分
		switch(imgTrsn.nonDevWidth) {
			case 200: getNowDrawIndex = 0; diic.drawCount = diic.senderRateIs1[0]; diic.changeBufferForDrawing = diic.senderRateIs1; break;
			case 400: getNowDrawIndex = 1; diic.drawCount = diic.senderRateIs2[0]; diic.changeBufferForDrawing = diic.senderRateIs2; break;
			case 600: getNowDrawIndex = 2; diic.drawCount = diic.senderRateIs3[0]; diic.changeBufferForDrawing = diic.senderRateIs3; break;
			case 800: getNowDrawIndex = 3; diic.drawCount = diic.senderRateIs4[0]; diic.changeBufferForDrawing = diic.senderRateIs4; break;
			case 1000: getNowDrawIndex = 4; diic.drawCount = diic.senderRateIs5[0]; diic.changeBufferForDrawing = diic.senderRateIs5; break;
			case 1200: getNowDrawIndex = 5; diic.drawCount = diic.senderRateIs6[0]; diic.changeBufferForDrawing = diic.senderRateIs6; break;
			case 1400: getNowDrawIndex = 6; diic.drawCount = diic.senderRateIs7[0]; diic.changeBufferForDrawing = diic.senderRateIs7; break;
			case 1600: getNowDrawIndex = 7; diic.drawCount = diic.senderRateIs8[0]; diic.changeBufferForDrawing = diic.senderRateIs8; break;
			case 1800: getNowDrawIndex = 8; diic.drawCount = diic.senderRateIs9[0]; diic.changeBufferForDrawing = diic.senderRateIs9; break;
		}
	}
	
	public boolean checkFile(String filePath) {
		File newFile = new File(filePath);
		if(newFile.exists()) { return true; }
		else { return false; }
	}
	
	public Bitmap scaleBitmap(Bitmap bitmap) {    
        int w = bitmap.getWidth(); // == 200
        int h = bitmap.getHeight();  // == 200
        Matrix matrix = new Matrix();    
        //圖片放大縮小主軸
        float sx= (float) (diau.getDeviceX / imgTrsn.ARMTouchImgX);    
        float sy= (float) (diau.getDeviceY / imgTrsn.ARMTouchImgY);    
        matrix.postScale(sx, sy);    
        Bitmap scaleBmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);    
        return scaleBmp;    
    }
	
	public boolean sdcardDetect() { return (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)); }
	
	public int getWidthCount() { return (imgTrsn.nonDevWidth/imgTrsn.devWidth); } //是 X 軸第幾張
	
	public int getHeightCount() { //是 Y 軸第幾張
		int locationY = 0;
		if(imgTrsn.nonDevHeight % imgTrsn.DevHeight == 0) { locationY = imgTrsn.nonDevHeight/imgTrsn.DevHeight; }
		else { locationY = (imgTrsn.nonDevHeight/imgTrsn.DevHeight) + 1; }
		return locationY;
	}
	
	public void draw(Canvas drawImg) {
		switch(refreahScreen) {
			case 2: break; //編輯模式用
			case 1:
				String filePath = "";
				drawEachTimeToGet();
				//必要時才 load 影像
				for(int i = 0; i < diic.drawCount ; i++) {
					drawEachTimeToGet();					
				    filePath = LocationPath + "/" + diic.changeBufferForDrawing[1+i] + ".webp"; //從 1 開始，0 紀錄有幾張
				    if(!checkFile(filePath)) { continue; }
				    if(diic.needToDrawIndex[getNowDrawIndex][diic.changeBufferForDrawing[1+i]] == 0 &&
				    		diic.flagToDraw[getNowDrawIndex][diic.changeBufferForDrawing[1+i]]++ < keepDraw) { continue; } //精確控制，假如沒有更新畫面，就免去檔案 I/O
				    try {
				    	bmp[diic.changeBufferForDrawing[1+i]] = BitmapFactory.decodeFile(filePath).copy(Bitmap.Config.ARGB_4444, true);
				    	bmp[diic.changeBufferForDrawing[1+i]] = scaleBitmap(bmp[diic.changeBufferForDrawing[1+i]]); //進行放大縮小
				    	diic.needToDrawIndex[getNowDrawIndex][diic.changeBufferForDrawing[1+i]] = 0;
				    	diic.flagToDraw[getNowDrawIndex][diic.changeBufferForDrawing[1+i]] = 0; //重設成 0
				    } catch (Exception e) { }
				} 
				drawImg.drawColor(Color.BLACK);
				for(int i = 0; i < diic.drawCount ; i++) {
					drawPieceCount = diic.drawCount; 
					try {
						drawImg.drawBitmap(bmp[diic.changeBufferForDrawing[1+i]], (int)(imgTrsn.recordMapXY[diic.changeBufferForDrawing[1+i]][0]*getImgScaleX), (int)(imgTrsn.recordMapXY[diic.changeBufferForDrawing[1+i]][1]*getImgScaleY), null);
						vBitmapCanvas.drawBitmap(bmp[diic.changeBufferForDrawing[1+i]], (int)(imgTrsn.recordMapXY[diic.changeBufferForDrawing[1+i]][0]*getImgScaleX), (int)(imgTrsn.recordMapXY[diic.changeBufferForDrawing[1+i]][1]*getImgScaleY), null);
					} catch (Exception e) { }
				}
				return ;
			case 0:
				drawImg.drawColor(Color.BLACK);
		    	for(int i = 0; i < drawPieceCount ; i++) { drawImg.drawBitmap(bmp[i], (int)(getRecordXY[i][0]*getImgScaleX), (int)(getRecordXY[i][1]*getImgScaleY), null); }
		    	break;
		}
	}
	
	private void drawCircle(int x, int y, Paint paint, Canvas c) { 
		c.drawCircle(x, y, 24, paint);
	}//單純畫圓，一般情況使用
	
	private void drawCircleEdit(float x, float y, Paint paint, Canvas c) {
		if(!(x2 < 0.00001 && y2 < 0.00001)) { vBitmapCanvas.drawLine(x, y, x2, y2, paint); }//畫至 bitmap 
		if(!(x3 < 0.00001 && y3 < 0.00001)) { vBitmapCanvas.drawLine(x2, y2, x3, y3, paint); }
		if(!(x2 < 0.00001 && y2 < 0.00001))  { c.drawLine(x, y, x2, y2, paint); }//畫至 bitmap
		if(!(x3 < 0.00001 && y3 < 0.00001)) { c.drawLine(x2, y2, x3, y3, paint); }
	}//單純畫圓，編輯模式使用
	
	public void transferPoint() {
		if(editModeFlag == 1) { return ; } //編輯模式下不用傳遞點訊息
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(siac.serIPSite);
			String getMsgToTransPoint = tI.getPoints(transferPointsCount, diau, tPI, imgTrsn);
			sendData = getMsgToTransPoint.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, siac.serverPort);
			//宣告準備傳送出去的 packet，並說明傳輸內容名稱(sendData)、長度(.length)及 server 的 port
			clientSocket.send(sendPacket);//傳送封包，將包含資料、長度及 port 等訊息，傳送出去
		} catch (SocketException e) { } 
		  catch (UnknownHostException e) { } 
		  catch (IOException e) { }
	}

	//-------------------------------------------------------------------------------------
	public Handler mHandlerTime = new Handler();
		
	private final Runnable timerRun = new Runnable() {
		public void run() {
			mHandlerTime.postDelayed(this, 5); //5 millsec for sensation
			if(editModeFlag == 1) { return ; } //暫時不要更新
			drawImg = getHolder().lockCanvas();
			if(drawImg != null) {
				draw(drawImg);
				if(flag == 1) {
					for(int i = 0; i < 4; i++) { 
						if(tPI.eachPointXY[i][0] == 0 && tPI.eachPointXY[i][1] == 0) { continue; }
						drawCircle(tPI.eachPointXY[i][0],tPI.eachPointXY[i][1],touchPaints[i],drawImg);
						tPI.eachPointXY[i][0] = tPI.eachPointXY[i][1] = 0; //重設至原點
					} 
					flag = 0;
				}
				getHolder().unlockCanvasAndPost(drawImg);
			}
		}
	};
	
	public void onDestory() { mHandlerTime.removeCallbacks(timerRun); }
	//-------------------------------------------------------------------------------------
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//開啟長觸碰的主軸
        if (gestureDetector.onTouchEvent(event)) { return true; }  
		//onTouchEvent 中取出點的事件
		int pointerCount = event.getPointerCount();
		flag = 1;
		//超過多點，就以最多點來處理
		if (pointerCount > MAX_TOUCHPOINTS) { pointerCount = MAX_TOUCHPOINTS; }
		tPI.pointCount = pointerCount; //紀錄有多少個點
		for (int i = 0; i < pointerCount; i++) {
			int id = event.getPointerId(i);
			if(id > 4) { break; } //控制僅有前四個點可以傳
			x = (int) event.getX(i);
			y = (int) event.getY(i);
			//可用於傳輸點
			tPI.eachPointXY[id][0] = x; // 0可以改成 id
			tPI.eachPointXY[id][1] = y;
			transferPoint();
			//-----------編輯區------------
			x_edit = event.getX(i);
			y_edit = event.getY(i);
			if(editModeFlag == 1) {
				drawImg = getHolder().lockCanvas();
				if(drawImg != null) {
					touchPaintsForEditing[chioceColor].setStrokeWidth(paintSize);
					drawCircleEdit(x_edit,y_edit,touchPaintsForEditing[chioceColor],drawImg);
					getHolder().unlockCanvasAndPost(drawImg);
				}
				drawBuffer = getHolder().lockCanvas(); // double buffer
				if(drawBuffer != null) {
					touchPaintsForEditing[chioceColor].setStrokeWidth(paintSize);
					drawCircleEdit(x_edit,y_edit,touchPaintsForEditing[chioceColor],drawBuffer);
					getHolder().unlockCanvasAndPost(drawBuffer);
				}
				if(x2 != x3) { x3 = x2; } //用於點之間的連接，以下順序不可換
				if(y2 != y3) { y3 = y2; }
				if(x_edit != x2) { x2 = x_edit; }
				if(y_edit != y2) { y2 = y_edit; }
			}
		}
		
		int touchEvent = event.getAction();
		switch (touchEvent) {  
	       case MotionEvent.ACTION_UP: x2 = y2 = x3 = y3 = 0; break;  
		}
		return true;
	} 
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

	public void surfaceCreated(SurfaceHolder holder) { }

	public void surfaceDestroyed(SurfaceHolder holder) { }

}