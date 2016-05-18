package com.ebanswers.attendance;

import com.example.db.DBAdapter;
import com.ebanswers.attendance.R;
import com.ebanswers.object.Person;
import com.example.util.AbstractActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class InformationActivity extends AbstractActivity implements OnClickListener {
    static  DBAdapter dbAdapter;
	private Button m_btnCancel;
	private defineTextView textView1, textView2, textView3, textView4, textView5;
   String strPersonCount, strAttendanceCount;
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        initUI(savedInstanceState, R.layout.informationstorage);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}

	@Override
	public void findView() {
		   Context  context = getApplicationContext();
	        
	        dbAdapter = new DBAdapter(context);
	        dbAdapter.open();
	        long attenDanceCount = dbAdapter.getCount("select count (*) from Attendance"); 
	        long count = dbAdapter.getCount("select count (*) from Person");
	       
	        m_btnCancel = (Button)findViewById(R.id.btnCancel);
	       	      
	        textView1 = (defineTextView)findViewById(R.id.textview1);
	        textView1.setPosition(100, 80);
	        textView1.setState(1);
	     
	        strAttendanceCount = ""+attenDanceCount+"  条";
	        textView1.setText("考勤记录数量", strAttendanceCount);
	        
	        textView2 = (defineTextView)findViewById(R.id.textview2);
	        textView2.setPosition(100, 80);
	        strPersonCount = ""+count+"  枚";
	        textView2.setText("指纹数量", strPersonCount);
	        
	        textView3 = (defineTextView)findViewById(R.id.textview3);
	        textView3.setPosition(100, 80);
	        textView3.setText("厂商", "上海豪普森生物识别应用科技有限公司");
	        
	        textView4 = (defineTextView)findViewById(R.id.textview4);
	        textView4.setPosition(100, 80);
	        textView4.setText("软件版本", "1.2.7");  //1.0.04无停止切换，更新连接bug
	       
	        textView5 = (defineTextView)findViewById(R.id.textview5);
	        textView5.setPosition(100, 80);
	        textView5.setText("设备ID", Secure.getString(getApplication().getApplicationContext().getContentResolver(), Secure.ANDROID_ID));
	      
	        dbAdapter.close();
	        dbAdapter = null;
	}

	@Override
	public void fillData() {
	}

	@Override
	public void setListener() {
		// TODO Auto-generated method stub
		  m_btnCancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
	}
}
