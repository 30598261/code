package com.example.util;

import com.ebanswers.attendance.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil
{
	 public static Toast mToast1, mToast2, waringMToast=null;
	 public static TextView text1, text2, text3, text4;
	 public static LayoutInflater inflater=null, waringInflater=null;
	 public static View layout=null, waringLayout=null;
	 
	 private static void destory()
	 {
		 if(mToast1 != null)
			{
				mToast1.cancel();
				mToast1 =null;
			}
			if(mToast2 !=null)
			{
				mToast2.cancel();
				mToast2 = null;
			}
			if(waringMToast != null)
			{
				waringMToast.cancel();
				waringMToast = null;
			}
		
	 }
	@SuppressLint("NewApi")
	public static void showToast3Text(Context context, String msg1, String msg2, String msg3, int duration, boolean birthdayTag) 
	 {
		if(ToastWaring.mToastWaring!=null)
		{
		 	ToastWaring.mToastWaring.cancel();
		 	ToastWaring.mToastWaring = null;
		}
		 	
		destory();
		
		if(mToast1 == null)
		{
		   inflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	       layout = inflater.inflate(R.layout.toast,  null);  
	       
	       //查找ImageView控件  
	       //注意是在layout中查找  
	       text1 = (TextView) layout.findViewById(R.id.textView1);  
	       text1.setText(msg1);
	       text2 = (TextView) layout.findViewById(R.id.TextView02);  
	       text2.setText(msg2);  
	       text3 = (TextView) layout.findViewById(R.id.TextView03);  
	       text3.setText(msg3); 
	   	  	  
	       if(msg1==null)
	       {
	    	   text1.setVisibility(View.GONE);
	       }
	       else
	    	   text1.setVisibility(View.VISIBLE);
	       
	       if(msg2==null)
	       {
	    	   text2.setVisibility(View.GONE);
	       }
	       else
	    	   text2.setVisibility(View.VISIBLE);
	       
	       if(msg3==null)
	       {
	    	   text3.setVisibility(View.GONE);
	       }
	       else
	    	   text3.setVisibility(View.VISIBLE);
	       
	       mToast1 = new Toast(context);
	       //设置Toast的位置  
	       mToast1.setGravity(Gravity.CENTER, 0, 280);  
	       mToast1.setDuration(duration);  
	       //让Toast显示为我们自定义的样子  
	       mToast1.setView(layout);  
		}
		else
		{
			 if(msg1==null)
			 {
				 text1.setVisibility(View.GONE);
			 }
		     else
		     {
		    	 text1.setVisibility(View.VISIBLE);
		    	 text1.setText(msg1);  
		     }
			 if(msg2==null)
			 {
				 text2.setVisibility(View.GONE);
			 }
			 else
			 {
			     text2.setVisibility(View.VISIBLE);
			     text2.setText(msg2); 
			 }
			 if(msg3==null)
			 {
			     text3.setVisibility(View.GONE);
			 }
			 else
			 {
			     text3.setVisibility(View.VISIBLE);
			     text3.setText(msg3); 
			 }
		 }
		 
		  if(msg1!=null&&msg2!=null&&msg3!=null)
	      {
	    	   layout.setBackgroundColor(context.getResources().getColor(R.color.green)); 
	      }
	      else
	      {
	    	   layout.setBackgroundColor(context.getResources().getColor(R.color.dkgray)); 
	    
	      }
		   text1.setTextColor(context.getResources().getColor(R.color.white));
    	   text2.setTextColor(context.getResources().getColor(R.color.white));
   	   	   text3.setTextColor(context.getResources().getColor(R.color.white));
   	   	  
		  if(birthdayTag==true)
		  {   //今天过生日
			  layout.setBackground(context.getResources().getDrawable(R.drawable.untitled));
			  text1.setTextColor(context.getResources().getColor(R.color.red));
			  text2.setTextColor(context.getResources().getColor(R.color.red));
		  	  text3.setTextColor(context.getResources().getColor(R.color.red));
		  	  text1.setPadding(10, 85, 10, 0);
		 	  mToast1.setGravity(Gravity.CENTER, 0, 220);  
		  }
		  else
			  mToast1.setGravity(Gravity.CENTER, 0, 280);  
	
 	   
 	   	 mToast1.show();
	 }
	 
	@SuppressLint("NewApi")
	public static void showToast3TextLongTime(Context context, String msg1, String msg2, String msg3, int duration, boolean birthdayTag) 
	 {
		if(ToastWaring.mToastWaring!=null)
		{
		 	ToastWaring.mToastWaring.cancel();
		 	ToastWaring.mToastWaring = null;
		}
		
		if(mToast2 !=null)
		{
			mToast2.cancel();
			mToast2 = null;
		}
		if(waringMToast != null)
		{
			waringMToast.cancel();
			waringMToast = null;
		}
		
		if(errMsgTag==true && mToast1!=null)
		{
			mToast1.cancel();
			mToast1 = null;
			errMsgTag = false;
		}
		
		if(mToast1 == null)
		{
		   inflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	       layout = inflater.inflate(R.layout.toast,  null);  
	       
	       //查找ImageView控件  
	       //注意是在layout中查找  
	       text1 = (TextView) layout.findViewById(R.id.textView1);  
	       text1.setText(msg1);
	       text2 = (TextView) layout.findViewById(R.id.TextView02);  
	       text2.setText(msg2);  
	       text3 = (TextView) layout.findViewById(R.id.TextView03);  
	       text3.setText(msg3); 
	   	  	  
	       if(msg1==null)
	       {
	    	   text1.setVisibility(View.GONE);
	       }
	       else
	    	   text1.setVisibility(View.VISIBLE);
	       
	       if(msg2==null)
	       {
	    	   text2.setVisibility(View.GONE);
	       }
	       else
	    	   text2.setVisibility(View.VISIBLE);
	       
	       if(msg3==null)
	       {
	    	   text3.setVisibility(View.GONE);
	       }
	       else
	    	   text3.setVisibility(View.VISIBLE);
	       
	       mToast1 = new Toast(context);
	       //设置Toast的位置  
	       mToast1.setGravity(Gravity.CENTER, 0, 280);  
	       mToast1.setDuration(duration);  
	       //让Toast显示为我们自定义的样子  
	       mToast1.setView(layout);  
		}
		else
		{
			 if(msg1==null)
			 {
				 text1.setVisibility(View.GONE);
			 }
		     else
		     {
		    	 text1.setVisibility(View.VISIBLE);
		    	 text1.setText(msg1);  
		     }
			 if(msg2==null)
			 {
				 text2.setVisibility(View.GONE);
			 }
			 else
			 {
			     text2.setVisibility(View.VISIBLE);
			     text2.setText(msg2); 
			 }
			 if(msg3==null)
			 {
			     text3.setVisibility(View.GONE);
			 }
			 else
			 {
			     text3.setVisibility(View.VISIBLE);
			     text3.setText(msg3); 
			 }
		 }
		 
		  if(msg1!=null&&msg2!=null&&msg3!=null)
	      {
	    	   layout.setBackgroundColor(context.getResources().getColor(R.color.green)); 
	      }
	      else
	      {
	    	   layout.setBackgroundColor(context.getResources().getColor(R.color.dkgray)); 
	    
	      }
		   text1.setTextColor(context.getResources().getColor(R.color.white));
    	   text2.setTextColor(context.getResources().getColor(R.color.white));
   	   	   text3.setTextColor(context.getResources().getColor(R.color.white));
   	   	  
		  if(birthdayTag==true)
		  {   //今天过生日
			  layout.setBackground(context.getResources().getDrawable(R.drawable.untitled));
			  text1.setTextColor(context.getResources().getColor(R.color.red));
			  text2.setTextColor(context.getResources().getColor(R.color.red));
		  	  text3.setTextColor(context.getResources().getColor(R.color.red));
		  	  text1.setPadding(10, 85, 10, 0);
		 	  mToast1.setGravity(Gravity.CENTER, 0, 220);   
		  }
		  else
		  {
			  mToast1.setGravity(Gravity.CENTER, 0, 280);
		  }
 	   	 mToast1.show();
	 }
	
	
	 public static void showToast(Context context, String msg, int duration) {
		 showToast3Text(context, msg, null, null, duration, false);
	 }
	 public static boolean errMsgTag = false;
	 public static void showToastTextLong(Context context, String msg1, int duration) {
		 	errMsgTag = true;
		 	if(ToastWaring.mToastWaring!=null)
		 	{
		 		ToastWaring.mToastWaring.cancel();
		 		ToastWaring.mToastWaring = null;
		 	}
			if(waringMToast != null)
			{
				waringMToast.cancel();
				waringMToast = null;
			}
			if(mToast1 == null)
			{
			   inflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
		       layout = inflater.inflate(R.layout.toast,  null);  
		       
		       //查找ImageView控件  
		       //注意是在layout中查找  
		       text1 = (TextView) layout.findViewById(R.id.textView1);  
		       text1.setText(msg1);
		       text2 = (TextView) layout.findViewById(R.id.TextView02);  
		       text3 = (TextView) layout.findViewById(R.id.TextView03);  
		     
		       if(msg1==null)
		       {
		    	   text1.setVisibility(View.GONE);
		       }
		       else
		    	   text1.setVisibility(View.VISIBLE);
		       
		       text2.setVisibility(View.GONE);
		       text3.setVisibility(View.GONE);
	
		       mToast1 = new Toast(context);
		       //设置Toast的位置  
		     
		       mToast1.setDuration(duration);  
		       //让Toast显示为我们自定义的样子  
		       mToast1.setView(layout);  
			 }
			 else
			 {
				 if(msg1==null)
				 {
					 text1.setVisibility(View.GONE);
				 }
			     else
			    	 text1.setVisibility(View.VISIBLE);
				 text2.setVisibility(View.GONE);
				 text3.setVisibility(View.GONE);
				 				 
				 text1.setText(msg1);  
			 }
			 
			  if(msg1.indexOf("认证未通过")>0)
		      {
		    	   layout.setBackgroundColor(context.getResources().getColor(R.color.red));
		    	   //layout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.red));
		      }
		      else
		      {
		    	   layout.setBackgroundColor(context.getResources().getColor(R.color.dkgray)); 
		      }
			  text1.setPadding(10, 0, 10, 0);
			  text2.setPadding(10, 0, 10, 0);
			  text3.setPadding(10, 0, 10, 0);
			  text1.setTextColor(context.getResources().getColor(R.color.white));
			  mToast1.setGravity(Gravity.CENTER, 0, 280);  
			  mToast1.show();
		 }
	 
	 
	 
	 @SuppressLint("NewApi")
	public static void showWarningMsg(Context context, String msg1, int duration) {

		 	if(ToastWaring.mToastWaring!=null)
		 	{
		 		ToastWaring.mToastWaring.cancel();
		 		ToastWaring.mToastWaring = null;
		 	}
		 	
		 if(mToast1 != null)
		 {
			mToast1.cancel();
			mToast1 =null;
		 }
		 if(mToast2 !=null)
		 {
			mToast2.cancel();
			mToast2 = null;
		 }
			
		 if(waringMToast == null)
		 {
		   waringInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	       waringLayout = waringInflater.inflate(R.layout.toast,  null);  
	      
	       //查找ImageView控件  
	       //注意是在layout中查找  
	       text1 = (TextView) waringLayout.findViewById(R.id.textView1);  
	       text2 = (TextView) waringLayout.findViewById(R.id.TextView02);  
	       text3 = (TextView) waringLayout.findViewById(R.id.TextView03);  
	     
	       text1.setVisibility(View.GONE);       
	       text2.setVisibility(View.GONE);
	       text3.setVisibility(View.GONE);

	       waringMToast = new Toast(context);
	       //设置Toast的位置  
	     
	       waringMToast.setDuration(duration);  
	       //让Toast显示为我们自定义的样子  
	       waringMToast.setView(waringLayout);  
		 }
		 else
		 {
			 text1.setVisibility(View.GONE);
			 text2.setVisibility(View.GONE);
			 text3.setVisibility(View.GONE);
		 }
		 
		 waringMToast.setGravity(Gravity.CENTER, 0, 250);
		 waringLayout.setBackground(context.getResources().getDrawable(R.drawable.warning));
		 waringMToast.show();
	 }
	 
	 
}


