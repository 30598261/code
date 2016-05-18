package com.ebanswers.attendance;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.Button;
import com.ebanswers.attendance.R;

public class defineImageButton extends Button {  
	public String text;
	public float textX=100,textY=80;
	public int color=Color.WHITE;
	public int size=30;
	private int state =0;
	private Bitmap bitmap;
	public String m_internetText;
	
	 public defineImageButton(Context context) {  
          super(context, null);  
	 }  
	public defineImageButton(Context context,AttributeSet attrs)
	{
		super(context,attrs);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
	
	}
	public defineImageButton(Context context, AttributeSet attrs,int defStyle) {  
        super(context, attrs, defStyle);  
}        // 注意实现带三个参数的构�?函数  
	//设置�?��显示的文�?
	public void setText(String text){
		this.text=text;
	//设置文本显示的颜色�?
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
    	if(text!=null)
    	{
    		paint.setARGB(255, 163, 165, 170);
    	}
    	Rect src = new Rect(0, 0, bitmap.getWidth(),bitmap.getHeight());
    	int x = (int) (bitmap.getWidth()*0.5);
    	int y = (int) (bitmap.getHeight()*0.5);
    	if(state==1)
    	{
    		canvas.drawText(text, textX, textY+13, paint);	
    		canvas.drawBitmap(bitmap, src, new Rect(1180, 60, 1180+x, 60+y), paint);
    	}
    	else if(state==5)
    	{
    		canvas.drawText(text, textX, textY+13, paint);	
    		paint.setTextSize(20);
    		if(m_internetText.equals("空") || m_internetText.equals("未连接"))
    		{
    			paint.setColor(Color.RED);
    		}
    		else
    		{
    			paint.setARGB(255, 163, 165, 170);
    		}
    		canvas.drawText(m_internetText, textX+950, textY+13, paint);	
    		paint.setTextSize(size);
    		paint.setARGB(255, 163, 165, 170);
    		canvas.drawBitmap(bitmap, src, new Rect(1180, 60, 1180+x, 60+y), paint);
    	}
    	else if(state==6)
    	{
    		if(text.equals("连接密码"))
    		{
    			paint.setColor(Color.RED);
    		}
    		else
    		{
    			paint.setARGB(255, 163, 165, 170);
    		}
    		canvas.drawText(text, textX, textY, paint);	
    		paint.setTextSize(20);
    		paint.setARGB(255, 163, 165, 170);
    		if(m_internetText.equals("空") || m_internetText.equals("未连接"))
    		{
    			paint.setColor(Color.RED);
    		}
    		else
    		{
    			paint.setARGB(255, 163, 165, 170);
    		}
    		canvas.drawText(m_internetText, textX+950, textY, paint);	
    		paint.setTextSize(size);
    		paint.setARGB(255, 163, 165, 170);
    		canvas.drawBitmap(bitmap, src, new Rect(1180, 50, 1180+x, 50+y), paint);
    	}
    	else
    	{
    		if(text.equals("网络设置"))
    		{
    			paint.setColor(Color.RED);
    			canvas.drawText(text, textX, textY, paint);	
        		canvas.drawBitmap(bitmap, src, new Rect(1180, 50, 1180+x, 50+y), paint);
    		}
    		else
    		{
    			canvas.drawText(text, textX, textY, paint);	
        		canvas.drawBitmap(bitmap, src, new Rect(1180, 50, 1180+x, 50+y), paint);
    		}
    	}
    }
	
	public void setState(int state)
	{
		this.state = state;
	}
	
	public void setInternetText(String text)
	{
		this.m_internetText = text;
	}
}
