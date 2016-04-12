package com.example.multitouchtransmission01;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MultiTouchTransmission01 extends Activity {
	
	//裝置資訊用
	DeviceInfoAndUser diau = new DeviceInfoAndUser();
	//主機資訊用
	ServerInfoAndCheck siac = new ServerInfoAndCheck();
	//UI使用
	private EditText edDeviceName, edDeviceID;
	private TextView tVDeviceHeight, tVDeviceWidth, tBShowServerIP;
	private Button bTReConnect, bTBgn, bTEnd, bTSetServer;
	//準備頁 UI 使用
	private TextView tVDevName_2, tVDevID_2, tVDevHeight_2, tVDevWidth_2, tVShowServerIP_2, tVShowCount_2;
	//控制旗標
	private int showServerIP = 0;
	
	public void getPixel() {
		DisplayMetrics metrics = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        diau.getDeviceX = metrics.widthPixels;
        diau.getDeviceY = metrics.heightPixels;
	}
	
	public String devName(String getAccountName) { //去除 google 帳戶 @ 之後的信箱地址
		String[] vElement = getAccountName.split("\\@");
		String usingName = vElement[0] + "@" + vElement[1];
		return vElement[0];
	}
	
	public void getDID() {
		//---------Way 1: 用手機的 Secure ID---------
		//diau.deviceName = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
		//---------Way 2: 用手機的 gmail 帳號---------
		AccountManager accountManager = AccountManager.get(this);
		Account[] accounts = accountManager.getAccountsByType("com.google");
		for(Account account : accounts) { diau.deviceName = devName(account.name); }
	}
	
	public void getCheck() {
	   	Connection reEstbConn = new Connection(); //取得連線資訊
	   	final int reCountServer = reEstbConn.countServer;
	   	if(reCountServer == 0) {
	 	   AlertDialog.Builder editDialog = new AlertDialog.Builder(this);
	       editDialog.setTitle("沒有 ARM Server");
	       final TextView eTgetMessage = new TextView(this);
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
	 	   AlertDialog.Builder UnitSelectionServerRe = new AlertDialog.Builder(this);
		   UnitSelectionServerRe.setTitle("選擇 Server");
		   UnitSelectionServerRe.setSingleChoiceItems(reServerSite, 0, new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int itemChoice) {
		            Toast.makeText(getApplicationContext(), reServerSite[itemChoice], Toast.LENGTH_SHORT).show();
		            for(int i = 0 ; i < reCountServer ; i++) { if(i == itemChoice) { siac.serIPSite = reServerSite[i]; if(showServerIP==1) { tBShowServerIP.setText(siac.serIPSite); } }}
		            dialog.dismiss(); }});
		    AlertDialog alertServer = UnitSelectionServerRe.create();
		    alertServer.show(); //創造出選單後，記得要 show() 出來
	   	}
    }
	
	public void setServerFun() {
		 AlertDialog.Builder editDialog = new AlertDialog.Builder(this);
	     editDialog.setTitle("輸入待確認的伺服器 IP");
	     final EditText eTgetMessage = new EditText(this);
	     editDialog.setView(eTgetMessage);
	     editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	  	     public void onClick(DialogInterface arg0, int arg1) {
	  	    	   String getMessageStr = eTgetMessage.getText().toString(); //取得輸入的文字
	  	    	   SetServerCheck ssc = new SetServerCheck(getMessageStr);
	  	    	   try { Thread.sleep(4000);
	  	    	   } catch (InterruptedException e) {}
	  	    	   if(ssc.checkServer == 1) { tBShowServerIP.setText(getMessageStr); siac.serIPSite = getMessageStr; Toast.makeText(MultiTouchTransmission01.this, getMessageStr, Toast.LENGTH_SHORT).show(); }
	    	   	   else { Toast.makeText(MultiTouchTransmission01.this, "此 Server 不存在", Toast.LENGTH_SHORT).show(); }
	  	     }});
	     editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	  	     public void onClick(DialogInterface arg0, int arg1) {  }});
	     editDialog.show();
	}
	
	public void componentConnect() {
		edDeviceName = (EditText) findViewById(R.id.eTName);
		edDeviceID = (EditText) findViewById(R.id.eTID);
		tVDeviceHeight = (TextView) findViewById(R.id.tVHeight);
		tVDeviceWidth = (TextView) findViewById(R.id.tVWidth);
		tBShowServerIP = (TextView) findViewById(R.id.tVServerIP); showServerIP = 1;
		bTReConnect = (Button) findViewById(R.id.bTRC);
		bTBgn = (Button) findViewById(R.id.bTBegin);
		bTEnd = (Button) findViewById(R.id.bTEnd);
		bTSetServer = (Button) findViewById(R.id.setServerBT);
	}

	public void componentConnect2() {
		tVDevName_2 = (TextView) findViewById(R.id.tVName_2);
		tVDevID_2 = (TextView) findViewById(R.id.tVID_2);
		tVDevHeight_2 = (TextView) findViewById(R.id.tVHeight_2);
		tVDevWidth_2 = (TextView) findViewById(R.id.tVWidth_2);
		tVShowServerIP_2 = (TextView) findViewById(R.id.tVServetIP_2);
		tVShowCount_2 = (TextView) findViewById(R.id.tVCountTime_2);
	}
	
	public void componentFun() {
		edDeviceName.setText(diau.deviceName);
		edDeviceID.setText(diau.deviceID);
		tVDeviceHeight.setText(String.valueOf(diau.getDeviceY));
		tVDeviceWidth.setText(String.valueOf(diau.getDeviceX));
		tBShowServerIP.setText(siac.serIPSite);
		bTReConnect.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0) { getCheck(); }
        });
		bTBgn.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0) { 
        		if((! edDeviceName.getText().toString().equals("")) && (! siac.serIPSite.equals(""))) {
        			try {
        				int getInt = Integer.parseInt(edDeviceID.getText().toString());
	        			if(edDeviceID.getText().length() == 9) {
	        				diau.deviceName = edDeviceName.getText().toString();
	        				diau.deviceID = edDeviceID.getText().toString();
	        				try {
	        					setContentView(new MTView(MultiTouchTransmission01.this,diau,siac)); //setContent 必須要在 request 及 getWindows() 之後，才能有全螢幕
	        				} catch(Exception e) {
	        					setContentView(R.layout.activity_multi_touch_transmission02); 
		        				componentConnect2(); componectFun2();
	        				}
	        			}
	        			else { edDeviceID.setText("需要 9 位的數字"); }
        			} catch (Exception e) { edDeviceID.setText("僅能輸入 9 位\"數字\"");  }
        		}
        		else if(siac.serIPSite.equals("")) { tBShowServerIP.setText("---先確認 ServerIP 已被搜尋---"); }
        	}
        });
		bTEnd.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0) { finish(); }
        });
		bTSetServer.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0) { setServerFun(); }
        });
	}
	
	public void createDirAndFile() {
		FileAndDirCreate fadc = new FileAndDirCreate(); //預先創造資料夾，降低影像錯誤
		fadc.createDict();	fadc.createFile();
	}
	
	public void componectFun2() {
		tVDevName_2.setText("裝置名稱為： " + diau.deviceName);
		tVDevID_2.setText("裝置    ID： " + diau.deviceID);
		tVDevHeight_2.setText("裝置高度： " + String.valueOf(diau.getDeviceY));
		tVDevWidth_2.setText("裝置寬度： " + String.valueOf(diau.getDeviceX));
		tVShowServerIP_2.setText("連線主機： " + siac.serIPSite);
		tVShowCount_2.setText("-------初始化失敗，請重啟應用程式-------");
		try { Thread.sleep(4000);
		} catch (InterruptedException e) {  }
	}
	
	public void initialFun() {
		getPixel(); //取得螢幕等資訊
		getDID(); //取得裝置的名稱
		componentConnect();
		componentFun();
		createDirAndFile();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_multi_touch_transmission01);
        getCheck();
        initialFun();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_multi_touch_transmission01, menu);
        return true;
    }
}
