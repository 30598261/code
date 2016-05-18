package com.ebanswers.attendance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

public class IPTextView extends defineTextView{

	 public IPTextView(Context context) {  
         super(context, null);  
	 }  
	public IPTextView(Context context,AttributeSet attrs)
	{
		super(context,attrs);	
	}
	public IPTextView(Context context, AttributeSet attrs,int defStyle) {  
       super(context, attrs, defStyle);  
	}
	protected void onDraw(Canvas canvas)
	{
		Paint paint=new Paint();
    	paint.setTextAlign(Paint.Align.LEFT);
    	paint.setColor(color);
    	if(color!=0)
    	{
    		paint.setColor(color);
    	}
    	if(size!=0)
    	{	
    		paint.setTextSize(size);
    	}
    	paint.setARGB(255, 163, 165, 170);
    
      	if(text1!=null)
        {
      		paint.setColor(Color.RED);
        	canvas.drawText(text1, textX, textY+13, paint);	
        }
      	if(text2!=null)
      	{
      		if(text2.equals("未连接"))
      		{
      			paint.setColor(Color.RED);
      			paint.setTextSize(20);
      			canvas.drawText(text2, 1050, textY+13, paint);
      		}
      		else
      		{
      			paint.setARGB(255, 163, 165, 170);
      			canvas.drawText(text2, 1010, textY+13, paint);
      		}
      	}
    }
}
