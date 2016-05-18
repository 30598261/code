package com.ebanswers.attendance;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;
import com.ebanswers.attendance.R;

public class defineTextView extends TextView {  
	public String text1 = null, text2=null;
	public float textX=100,textY=80;
	public int color=Color.WHITE;
	public int size=30;
	private int state =0;
	
	 public defineTextView(Context context) {  
          super(context, null);  
	 }  
	public defineTextView(Context context,AttributeSet attrs)
	{
		super(context,attrs);	
	}
	public defineTextView(Context context, AttributeSet attrs,int defStyle) {  
        super(context, attrs, defStyle);  
}  
	public void setText(String text1, String text2){
		this.text1=text1;
		this.text2=text2;
}
	public void setColor(int color)
	{
		this.color=color;
	}
	
	public void setPosition(float XX,float YY)
    {
		textX=XX;
		textY=YY;
	}
	public void setSize(int XX)
    {
		size = XX;
	}
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
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
      	if(state==1)
      	{
      		if(text1!=null)
        	{
        		canvas.drawText(text1, textX, textY+13, paint);	
        	}
      		if(text2!=null)
      		{
      			canvas.drawText(text2, 980, textY+13, paint);
      		}
      	}
    	else
    	{
    		if(text1!=null)
        	{
        		canvas.drawText(text1, textX, textY, paint);	
        	}
    		if(text2!=null)
    		{
    			if(text2.equals("上海豪普森生物识别应用科技有限公司")==true)
    				canvas.drawText(text2, 700,textY, paint);
    			else
    			canvas.drawText(text2, 980,textY, paint);
    		}
    	}
    }
	
	public void setState(int state)
	{
		this.state = state;
	}
	
	
}
