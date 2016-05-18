package com.example.util;

import com.ebanswers.attendance.R;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastWaring
{
	public static Toast mToastWaring;
	private static TextView text1, text3, text4, text5;
	private static LayoutInflater inflater=null;
	private static View layout=null;
	
	public static void showToast(Context context, String msg1, String msg3, String msg4, String msg5,int duration) 
	 {
		 if(ToastUtil.mToast1 != null)
		 {
			 ToastUtil.mToast1.cancel();
			 ToastUtil.mToast1 =null;
		 }
		 if(ToastUtil.mToast2 !=null)
		 {
			 ToastUtil.mToast2.cancel();
			 ToastUtil.mToast2 = null;
		 }
		 
			if(ToastUtil.waringMToast != null)
			{
				ToastUtil.waringMToast.cancel();
				ToastUtil.waringMToast = null;
			}
		 
		if(mToastWaring != null)
		{
			mToastWaring.cancel();
			mToastWaring =null;
		}
		if(mToastWaring == null)
		{
		   inflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	       layout = inflater.inflate(R.layout.toast2,  null);  
	       //查找ImageView控件  
	       //注意是在layout中查找  
	       text1 = (TextView) layout.findViewById(R.id.textView1);  
	       text1.setText(msg1);
	       text3 = (TextView) layout.findViewById(R.id.TextView03); 
	       text3.setText(msg3);
	       text4 = (TextView) layout.findViewById(R.id.TextView04);  
	       text4.setText(msg4);
	       text5 = (TextView) layout.findViewById(R.id.TextView05); 
	       text5.setText(msg5);
	       mToastWaring = new Toast(context);
	       //设置Toast的位置  
	       mToastWaring.setGravity(Gravity.CENTER, 0, 250);  
	       mToastWaring.setDuration(duration);  
	       //让Toast显示为我们自定义的样子  
	       mToastWaring.setView(layout);  
		}
		else
		{
		    text1.setText(msg1);  
		    text3.setText(msg3);
		    text4.setText(msg4);
		    text5.setText(msg5);
		 }
		 text1.setTextColor(Color.RED);
		 text3.setTextColor(context.getResources().getColor(R.color.white));
		 text4.setTextColor(context.getResources().getColor(R.color.white));
		 text5.setTextColor(context.getResources().getColor(R.color.white));  
		 text1.setBackgroundColor(Color.rgb(255,227,21));
		 text3.setBackgroundColor(context.getResources().getColor(R.color.green));
		 text4.setBackgroundColor(context.getResources().getColor(R.color.green));
		 text5.setBackgroundColor(context.getResources().getColor(R.color.green));
		 mToastWaring.setGravity(Gravity.CENTER, 0, 250);  
 	     mToastWaring.show();
	 }
	
}