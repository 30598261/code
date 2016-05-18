package com.ebanswers.attendance;

import com.example.db.DBAdapter;
import com.ebanswers.attendance.R;
import com.example.util.AbstractActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class SystemsetActivity extends AbstractActivity {
	
	defineImageButton clearAllAttendanceBtn, clearAllUserBtn, clearNetSetBtn, resumeSetBtn;
	private Button m_btnCancel, pop_curBtn, pop_cancelBtn;
	static DBAdapter dbAdapter;
	SharedPreferences sharedPreferences;
	private AlertDialog popDialog;
	private TextView textView2;
	private int buttonState=0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI(savedInstanceState, R.layout.systemset);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

	@Override
	public void findView() {
		// TODO Auto-generated method stub
		m_btnCancel = (Button) findViewById(R.id.btnCancel);
		
		clearAllAttendanceBtn = (defineImageButton)findViewById(R.id.clearAllAttendanceID);
		clearAllAttendanceBtn.setPosition(100, 80);
		clearAllAttendanceBtn.setState(1);
		clearAllAttendanceBtn.setText("清空所有考勤记录");
		
		clearAllUserBtn = (defineImageButton)findViewById(R.id.clearAllUserBtnID);
		clearAllUserBtn.setPosition(100, 80);
		clearAllUserBtn.setText("清空所有指纹");
		
		//clearNetSetBtn = (defineImageButton)findViewById(R.id.clearNetSetBtnID);
		//clearNetSetBtn.setPosition(100, 80);
		//clearNetSetBtn.setText("恢复出厂网络设置");
		
		resumeSetBtn = (defineImageButton)findViewById(R.id.resumeSetBtnID);
		resumeSetBtn.setPosition(100, 83);
		resumeSetBtn.setText("恢复出厂设置");
	//	ipAddrTextView = (TextView)findViewById(R.id.ipAddr);
		//弹出对话框
		View popWindowView = View.inflate(this, R.layout.clearpopwindow, null);
		popDialog = new AlertDialog.Builder(this).create();
		popDialog.setView(popWindowView);
		pop_curBtn = (Button) popWindowView.findViewById(R.id.button1);
		pop_cancelBtn = (Button) popWindowView.findViewById(R.id.button2);
		textView2 = (TextView)popWindowView.findViewById(R.id.textView2);
	}

	@Override
	public void fillData() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setListener() {
		pop_curBtn.setOnClickListener(new Button.OnClickListener(){   
            public void onClick(View v)  
            {  
            	switch(buttonState)
            	{
            	case 1:
            		//删除考勤记录
                	dialog();
                	SendMessage("清空所有考勤记录成功");
                	SendSoundMsg(MessageAction.MESSAGE_PLAY_OPERATIONSUCC_SOUND);
                	popDialog.dismiss();	
            		break;
            	case 2:
            		DeleteAllPersonData(1);
            	//	SendMessage("清空所有指纹用户成功");
            		popDialog.dismiss();	
            		break;
            		/*
            	case 3:
            		ResetSystemSet();
     				SendMessage("恢复网络设置成功");
     				popDialog.dismiss();	
            		break;
            		*/
            	case 4:
            	            		
            		//删除考勤记录
                	dialog();
                	//删除所有考勤用户
                	DeleteAllPersonData(2);
            		//恢复网络设设置
            		ResetSystemSet();
            		
            		SendMessage("恢复出厂设置成功");
            	
            		Intent intent = new Intent();
            		intent.setAction(MessageAction.MESSAGE_PLAY_EXIT_SOUND);
            		sendBroadcast(intent);
                	popDialog.dismiss();
            		break;
            	default:
            		break;
            	}
            	ResetChangeTime();
            }  
        });  
		pop_cancelBtn.setOnClickListener(new Button.OnClickListener(){   
            public void onClick(View v)  
            {  
            	ResetChangeTime();
            	popDialog.dismiss();
            }  
        });  
		// TODO Auto-generated method stub
		clearAllAttendanceBtn.setOnClickListener(new Button.OnClickListener(){   
            public void onClick(View v)  
            {  
            	ResetChangeTime();

            	buttonState=1;
            	textView2.setText("是否要清空所有考勤记录");
				Window w=popDialog.getWindow();
				WindowManager.LayoutParams lp =w.getAttributes();
				lp.x=0;
				lp.y=-100;
				w.setAttributes(lp); 
				popDialog.show();
				
				SendSoundMsg(MessageAction.MESSAGE_PLAY_NOTES_SOUND);
            }  
        });  
	
		clearAllUserBtn.setOnClickListener(new Button.OnClickListener(){   
            public void onClick(View v)  
            {    
            	ResetChangeTime();
            	buttonState=2;
            	textView2.setText("是否要清空所有指纹用户");
				Window w=popDialog.getWindow();
				WindowManager.LayoutParams lp =w.getAttributes();
				lp.x=0;
				lp.y=-100;
				w.setAttributes(lp); 
				popDialog.show();
				
				SendSoundMsg(MessageAction.MESSAGE_PLAY_NOTES_SOUND);
            }  
        });  
		/*
		clearNetSetBtn.setOnClickListener(new Button.OnClickListener(){   
			public void onClick(View v)  
            {    
				ResetChangeTime();
				buttonState=3;
            	textView2.setText("是否恢复出厂网络设置");
				Window w=popDialog.getWindow();
				WindowManager.LayoutParams lp =w.getAttributes();
				lp.x=0;
				lp.y=-100;
				w.setAttributes(lp); 
				popDialog.show();
            }  
        });
        */ 
		
		resumeSetBtn.setOnClickListener(new Button.OnClickListener(){   
            public void onClick(View v)  
            {  
            	ResetChangeTime();
            	buttonState=4;
            	textView2.setText("是否恢复出厂设置");
				Window w=popDialog.getWindow();
				WindowManager.LayoutParams lp =w.getAttributes();
				lp.x=0;
				lp.y=-100;
				w.setAttributes(lp); 
				popDialog.show();
				SendSoundMsg(MessageAction.MESSAGE_PLAY_RECOVER_DEVICE);
			
            }  
        }); 
		
		m_btnCancel.setOnClickListener(new Button.OnClickListener(){   
            public void onClick(View v)  
            {  
            	finish();
            }  
        }); 
	}

	@Override
	public Bundle saveData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadData(Bundle bundle) {
		// TODO Auto-generated method stub
	//	String ipAddr = "IP : "+PooledRemoteFileServer.g_tcpIP;
		
	//	ipAddrTextView.setText(ipAddr);
	}
	
	
	protected void dialog() {
			dbAdapter = new DBAdapter(getApplicationContext());
	    	//打开数据库
		    dbAdapter.open();
		    dbAdapter.deleteTable("Attendance");
		    dbAdapter.exec("vacuum");
			dbAdapter.close();
			dbAdapter=null;
			
		}
	
    public void SendMessage(String str)
    {
    	/*
    	Intent intent = new Intent();
		intent.setAction(MessageAction.MESSAGE_SHOW_TEST);
		intent.putExtra("msg", str);
		*/
		
		Intent intent = new Intent();
		intent.setAction(MessageAction.MESSAGE_SHOW_TEST_LONG);
		intent.putExtra("pkg", getPackageName());
		intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.putExtra("msg1", str);
		sendBroadcast(intent);
    }
    
    
    void ResetSystemSet()
    {
    	  sharedPreferences = getApplicationContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE); 
          Editor editor = sharedPreferences.edit();  
		     
          editor.putString("LocalIPAddr","127.0.0.1");  
          editor.putString("PassWord", "");
          editor.putInt("voice", 4);
          editor.commit();  
          //声音恢复出厂设置
  		Intent intent = new Intent();
		intent.setAction(MessageAction.MESSAGE_PLAY_SET_SOUND);
		sendBroadcast(intent);
    }
    
    void DeleteAllPersonData(int id)
    {
    	//删除所有用户
    	dbAdapter = new DBAdapter(getApplicationContext());
	    dbAdapter.open();
	    dbAdapter.deleteTable("Person");
	    dbAdapter.exec("DELETE FROM sqlite_sequence;");
	    dbAdapter.exec("vacuum");
		dbAdapter.close();
		dbAdapter=null;
		SendResetDeviceMsg(id);
		ResetChangeTime();
    }
	
    void SendResetDeviceMsg(int id)
    {
    	if(id==1)
    	{
	    	//发送指纹消息
			Intent intent = new Intent();
			intent.setAction(MessageAction.MESSAGE_CLEAR_FINGER);
			sendBroadcast(intent);
    	}
    	else
    	{
    		//发送指纹消息
    		Intent intent = new Intent();
    		intent.setAction(MessageAction.MESSAGE_RECOVER_DEVICE);
    		sendBroadcast(intent);
    	}
    }
    
    void SendSoundMsg(String action)
    {
    	Intent intent = new Intent();
    	intent.setAction(action);
		sendBroadcast(intent);
    }
    
}
