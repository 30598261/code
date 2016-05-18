package com.ebanswers.attendance;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.ebanswers.attendance.R;

public class MyImageView extends ImageView{
	public float m_x;
	public float m_y;

	public MyImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	 public MyImageView(Context context, AttributeSet attrs,  
	            int defStyle) {  
	        super(context, attrs, defStyle);  
	        // TODO Auto-generated constructor stub  
	    }  
	  
	    public MyImageView(Context context, AttributeSet attrs) {  
	        super(context, attrs);  
	        // TODO Auto-generated constructor stub  
	    }  
	    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
	
		m_x = event.getX();
		m_y = event.getY();
		return super.onTouchEvent(event);
	}

}
