package com.ebanswers.attendance;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.ebanswers.attendance.R;
import com.example.util.AbstractActivity;
import com.example.util.ToastUtil;
public class fingerTestWeb extends AbstractActivity {
	defineImageButton setFingerButton, addAdminUser, clearNormalUserButton, clearAdminUserButton;
	private int m_admin;
	private Button m_btnCancel, addNormalCurButton1, addNormalCurButton2, addNormalNameCurButton1, addNormalNameCurButton2;   
	private EditText editText1, editTextName;
	private TextView textView2, textView3, textViewName2;
	private int buttonState=0;
	AlertDialog menuDialog, menuDialogName;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initUI(savedInstanceState, R.layout.fingertestweb);	
	}
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		menuDialog.dismiss();
		menuDialogName.dismiss();	
	}

	@Override
	public void findView() {
		// TODO Auto-generated method stub
		setFingerButton = (defineImageButton)findViewById(R.id.setFingerButton);
		setFingerButton.setPosition(100, 80);
		setFingerButton.setState(1);
		setFingerButton.setText("添加普通用户指纹");
		
		addAdminUser = (defineImageButton)findViewById(R.id.addAdminUserID);
		addAdminUser.setPosition(100, 80);
		addAdminUser.setText("添加管理员用户指纹");
		
		clearNormalUserButton = (defineImageButton)findViewById(R.id.clearNormalUserID);
		clearNormalUserButton.setPosition(100, 80);
		clearNormalUserButton.setText("删除普通用户指纹");
		
		clearAdminUserButton = (defineImageButton)findViewById(R.id.deleteAdminUserID);
		clearAdminUserButton.setPosition(100, 80);
		clearAdminUserButton.setText("删除管理员用户指纹");
		
	//	ipAddrTextView = (TextView)findViewById(R.id.ipAddr);
		m_btnCancel = (Button) findViewById(R.id.btnCancel);
		
		//弹出带Name对话框
		View popWindowView = View.inflate(this, R.layout.adduserpopname, null);
		menuDialogName = new AlertDialog.Builder(this).create();
		menuDialogName.setView(popWindowView);
		addNormalNameCurButton1 = (Button) popWindowView.findViewById(R.id.button1);
		addNormalNameCurButton2 = (Button) popWindowView.findViewById(R.id.button2);
		textViewName2 = (TextView)popWindowView.findViewById(R.id.textView2);
		editTextName = (EditText)popWindowView.findViewById(R.id.editTextName);
		
		//弹出无Name对话框
		View popWindow = View.inflate(this, R.layout.adduserpopwindow, null);
		menuDialog = new AlertDialog.Builder(this).create();
		menuDialog.setView(popWindow);
		addNormalCurButton1 = (Button) popWindow.findViewById(R.id.button1);
		addNormalCurButton2 = (Button) popWindow.findViewById(R.id.button2);
		textView2 = (TextView)popWindow.findViewById(R.id.textView2);
		textView3 = (TextView)popWindow.findViewById(R.id.textView3);
		editText1 = (EditText)popWindow.findViewById(R.id.editText1);

		
		editText1.setOnClickListener(new View.OnClickListener(){
			    @Override
			     public void onClick(View v) {
			    	ResetChangeTime();
			       }
			 });
		
		editText1.addTextChangedListener(new TextWatcher(){ 
	          
            @Override 
            public void onTextChanged(CharSequence s, int start, int before, 
                    int count) { 
            	ResetChangeTime();
            }

			@Override
			public void afterTextChanged(Editable s) {
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			} 
        }); 
		
		editTextName.setOnClickListener(new View.OnClickListener(){
		    @Override
		     public void onClick(View v) {
		    	ResetChangeTime();
		       }
		 });
		
		editTextName.addTextChangedListener(new TextWatcher(){ 
	          
            @Override 
            public void onTextChanged(CharSequence s, int start, int before, 
                    int count) { 
            	ResetChangeTime();
            }

			@Override
			public void afterTextChanged(Editable s) {
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			} 
             
        }); 
	}

	@Override
	public void fillData() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setListener() {
		// TODO Auto-generated method stub
		
		setFingerButton.setOnClickListener(new OnClickListener(){  
			@Override
			public void onClick(View v) {
				textViewName2.setText("添加普通指纹用户");
				//textViewName3.setText("     ID : ");
				//editTextName1.setText("");
				editTextName.setText("");
				Window w=menuDialogName.getWindow();
				WindowManager.LayoutParams lp =w.getAttributes();
				lp.x=0;
				lp.y=-200;
				w.setAttributes(lp);  
				
				buttonState = 1;
				menuDialogName.show();
				SendSoundMsg(MessageAction.MESSAGE_PLAY_INPUT_NAME);
				ResetChangeTime();
		}  
       });
		
		addAdminUser.setOnClickListener(new OnClickListener(){  
			@Override
			public void onClick(View v) {
				ResetChangeTime();
				textViewName2.setText("添加管理员指纹用户");
				//textViewName3.setText("     ID : ");
				//editTextName1.setText("");
				editTextName.setText("");
				Window w=menuDialogName.getWindow();
				WindowManager.LayoutParams lp =w.getAttributes();
				lp.x=0;
				lp.y=-100;
				w.setAttributes(lp);  
				buttonState = 2;
				SendSoundMsg(MessageAction.MESSAGE_PLAY_INPUT_NAME);
				menuDialogName.show();
				ResetChangeTime();
		}  
       });  
       
	

		addNormalCurButton1.setOnClickListener(new OnClickListener(){  
			@Override
			public void onClick(View v) {
				if(buttonState == 3)
				{
					ResetChangeTime();
					Intent intent = new Intent();
					intent.setAction(MessageAction.MESSAGE_DELETE_FINGER);
					
					if("".equals(editText1.getText().toString().trim()))
					{
						ToastUtil.showToast(getApplicationContext(), "ID号不能为空，请重新输入！", Toast.LENGTH_SHORT);
						return;
					}
					else
					{
						
						int id= Integer.parseInt(editText1.getText().toString());
						if(id >= 4 && id<=499)
						{	
							intent.putExtra("id", id);
							
							menuDialog.dismiss();
							sendBroadcast(intent);	
						}
						else
						{
							SendSoundMsg(MessageAction.MESSAGE_PLAY_DELLIMIT_SOUND);
							ToastUtil.showToast(getApplicationContext(), "普通用户有效ID号范围为4-499，请重新输入！", Toast.LENGTH_SHORT);
							return;
						}
					}
				}
				else if(buttonState == 4)
				{
					ResetChangeTime();
					Intent intent = new Intent();
					intent.setAction(MessageAction.MESSAGE_DELETE_FINGER);
					
					if("".equals(editText1.getText().toString().trim()))
					{
						ToastUtil.showToast(getApplicationContext(), "ID号不能为空，请重新输入！", Toast.LENGTH_SHORT);
						return;
					}
					else
					{
						int id= Integer.parseInt(editText1.getText().toString());
						if(id >= 1 && id<=3)
						{	
							intent.putExtra("id", id);
							
							menuDialog.dismiss();
							sendBroadcast(intent);	
						}
						else
						{
							SendSoundMsg(MessageAction.MESSAGE_PLAY_DELLIMIT_SOUND);
							ToastUtil.showToast(getApplicationContext(), "管理员有效ID号范围为1-3，请重新输入！", Toast.LENGTH_SHORT);
							return;
						}
					}
				}
		}  
       });  
		
		addNormalCurButton2.setOnClickListener(new OnClickListener(){  
			@Override
			public void onClick(View v) {
				ResetChangeTime();
				menuDialog.dismiss();
			}  
       }); 
		
		addNormalNameCurButton1.setOnClickListener(new OnClickListener(){  
			@Override
			public void onClick(View v) {
				if(buttonState == 1)
				{
					ResetChangeTime();
					Intent intent = new Intent();
					intent.setAction(MessageAction.MESSAGE_SET_FINGER);
					m_admin = 1;
					intent.putExtra("msg", m_admin);
					//id 
					/*
					if("".equals(editTextName1.getText().toString().trim()))
					{
						ToastUtil.showToast(getApplicationContext(), "ID号不能为空，请重新输入！", Toast.LENGTH_SHORT);
						return;
					}
					else
					{
						int id= Integer.parseInt(editTextName1.getText().toString());					
						if(id <= 3 || id>999)
						{
							ToastUtil.showToast(getApplicationContext(), "普通用户有效ID号范围为4-999，请重新输入！", Toast.LENGTH_SHORT);
							return;
						}
						intent.putExtra("fingerID", id);
					}
					*/
					//姓名
					if("".equals(editTextName.getText().toString()))
					{
						ToastUtil.showToast(getApplicationContext(), "姓名不能为空，请重新输入！", Toast.LENGTH_SHORT);
						return;
					}
					else
					{//System.out.println(editTextName.getText().toString());
						intent.putExtra("fingerID", -1);
						intent.putExtra("Name", editTextName.getText().toString());
						/*
						  //反射
						try {
							Field field = menuDialogName.getClass().getSuperclass().getDeclaredField("mShowing");
							field.setAccessible(true);
							field.set(menuDialogName, false);
						} catch (NoSuchFieldException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						*/
						menuDialogName.dismiss();
						sendBroadcast(intent);	
					}		
				}
				else if(buttonState == 2)
				{
					ResetChangeTime();
					m_admin = 0;
					Intent intent = new Intent();
					intent.setAction(MessageAction.MESSAGE_SET_FINGER);
					intent.putExtra("msg", m_admin);
					//id 
					/*
					if("".equals(editTextName1.getText().toString().trim()))
					{
						ToastUtil.showToast(getApplicationContext(), "ID号不能为空，请重新输入！", Toast.LENGTH_SHORT);
						return;
					}
					else
					{
						int id= Integer.parseInt(editTextName1.getText().toString());
						if(id >= 1 && id<=3)
						{	
							intent.putExtra("fingerID", id);
							
					//		menuDialog.dismiss();
					//		sendBroadcast(intent);	
						}
						else
						{
							ToastUtil.showToast(getApplicationContext(), "管理员有效ID号范围为1-3，请重新输入！", Toast.LENGTH_SHORT);
							return;
						}
					}
					*/
					intent.putExtra("fingerID", -2);
					//姓名
					if("".equals(editTextName.getText().toString()))
					{
						ToastUtil.showToast(getApplicationContext(), "姓名不能为空，请重新输入！", Toast.LENGTH_SHORT);
						return;
					}
					else
					{
						intent.putExtra("Name", editTextName.getText().toString());
						
						menuDialogName.dismiss();
						sendBroadcast(intent);	
					}	
				}
				else if(buttonState == 3)
				{
					ResetChangeTime();
					Intent intent = new Intent();
					intent.setAction(MessageAction.MESSAGE_DELETE_FINGER);
					
					if("".equals(editText1.getText().toString().trim()))
					{
						ToastUtil.showToast(getApplicationContext(), "ID号不能为空，请重新输入！", Toast.LENGTH_SHORT);
						return;
					}
					else
					{
						int id= Integer.parseInt(editText1.getText().toString());
						if(id >= 4 && id<=499)
						{	
							intent.putExtra("id", id);
							
							menuDialogName.dismiss();
							sendBroadcast(intent);	
						}
						else
						{
							SendSoundMsg(MessageAction.MESSAGE_PLAY_DELLIMIT_SOUND);
							ToastUtil.showToast(getApplicationContext(), "普通用户有效ID号范围为4-499，请重新输入！", Toast.LENGTH_SHORT);
							return;
						}
					}
				}
				else if(buttonState == 4)
				{
					ResetChangeTime();
					Intent intent = new Intent();
					intent.setAction(MessageAction.MESSAGE_DELETE_FINGER);
					
					if("".equals(editText1.getText().toString().trim()))
					{
						ToastUtil.showToast(getApplicationContext(), "ID号不能为空，请重新输入！", Toast.LENGTH_SHORT);
						return;
					}
					else
					{
						int id= Integer.parseInt(editText1.getText().toString());
						if(id >= 1 && id<=3)
						{	
							intent.putExtra("id", id);
							
							menuDialogName.dismiss();
							sendBroadcast(intent);	
						}
						else
						{
							SendSoundMsg(MessageAction.MESSAGE_PLAY_DELLIMIT_SOUND);
							ToastUtil.showToast(getApplicationContext(), "管理员有效ID号范围为1-3，请重新输入！", Toast.LENGTH_SHORT);
							return;
						}
					}
				}
		}  
       });  
		addNormalNameCurButton2.setOnClickListener(new OnClickListener(){  
			@Override
			public void onClick(View v) {
				ResetChangeTime();
				menuDialogName.dismiss();
			}  
       }); 
		
		
			
		clearNormalUserButton.setOnClickListener(new OnClickListener(){  
			@Override
			public void onClick(View v) {
				ResetChangeTime();
				textView2.setText("删除普通指纹用户");
				textView3.setText("普通用户   ID : ");
				editText1.setText("");
				Window w=menuDialog.getWindow();
				WindowManager.LayoutParams lp =w.getAttributes();
				lp.x=0;
				lp.y=-100;
				w.setAttributes(lp);  
				buttonState = 3;
				menuDialog.show();
				
				SendSoundMsg(MessageAction.MESSAGE_PLAY_INPUTID_SOUND);
		}  
       });  
		
		clearAdminUserButton.setOnClickListener(new OnClickListener(){  
			@Override
			public void onClick(View v) {
				ResetChangeTime();
				textView2.setText("删除管理员指纹用户");
				textView3.setText("管理员  ID : ");
				editText1.setText("");
				Window w=menuDialog.getWindow();
				WindowManager.LayoutParams lp =w.getAttributes();
				lp.x=0;
				lp.y=-100;
				w.setAttributes(lp);  
				buttonState = 4;
				menuDialog.show();
				SendSoundMsg(MessageAction.MESSAGE_PLAY_INPUTID_SOUND);
		}  
       });  
						
		m_btnCancel.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				ResetChangeTime();
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
	
	void SendSoundMsg(String action)
	{
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent);
	}
	

}
