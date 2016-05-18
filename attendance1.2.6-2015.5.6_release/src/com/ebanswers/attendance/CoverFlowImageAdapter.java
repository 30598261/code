package com.ebanswers.attendance;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import com.ebanswers.attendance.R;

public class CoverFlowImageAdapter extends BaseAdapter {
	private Context mContext;
	private Integer[] mImageIds = null;
	private Class<?>[] target=null;
	public boolean tag = false;
	static HashMap m = new HashMap();
	static HashMap daoPic = new HashMap();
	
	public Integer[] getmImageIds() {
		return mImageIds;
	}

	public Class<?>[] getTarget() {
		return target;
	}

	public CoverFlowImageAdapter(Context c, Integer[] mImageIds, Class<?>[] target) {
		mContext = c;
		this.mImageIds = mImageIds;
		this.target = target;
	}

	@Override
	public int getCount() {
		return mImageIds.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		
		 ImageView imageView = new ImageView(mContext);  
	
		/*
		//优化二，通过取余来循环取得imageIDs数组中的图像资源ID，取余可以大大较少资源的浪费
		imageView.setImageResource(mImageIds[position%mImageIds.length]);
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		imageView.setLayoutParams(new Gallery.LayoutParams(240, 150));//把图片缩小原来的60%
		*/
		 
				// 然后在你的getview中：
				 View layout = (View) m.get(position);
				 if (layout != null)
				 {
				      return layout;             
				 }
				 else
				 {  
				        Bitmap a = (Bitmap) daoPic.get(position);
						 if (a != null)
						 {
						       imageView.setImageBitmap(a);  
						       imageView.setLayoutParams(new Gallery.LayoutParams(320, 200));    
						 }
						 else
						 {  
						//	   System.out.println("#############  新加载图片 ############");
							   BitmapFactory.Options options = new BitmapFactory.Options();
					 		   //options.inSampleSize = 2 ;
							   //创建BitMap对象，用于显示图片     
						       Bitmap bitmap1 = BitmapFactory.decodeResource(mContext.getResources(), mImageIds[position], options);
						       Bitmap bitmap2 = MyImgView.createReflectedImage(bitmap1);
						       imageView.setImageBitmap(bitmap2);  
						       imageView.setLayoutParams(new Gallery.LayoutParams(320, 200));
						       bitmap1.recycle();
						       bitmap1 = null;
						       System.gc();
						       //放入集合。
						       daoPic.put(position, bitmap2);
						 }
				 }
		
				 /*
		 		   BitmapFactory.Options options = new BitmapFactory.Options();
		 		   options.inSampleSize = 2 ;
				   //创建BitMap对象，用于显示图片     
			       Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), mImageIds[position], options);
			       imageView.setImageBitmap(MyImgView.createReflectedImage(bitmap));  
			       imageView.setLayoutParams(new Gallery.LayoutParams(320, 200));
			       bitmap.recycle();
			       bitmap = null;
			       System.gc();
			        m.put(position, imageView);
			       */
			       //放入集合。
			   //   
			       
				 return imageView;  
	
	}

	public float getScale(boolean focused, int offset) {
		return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
	}
	
	/**
	 * 为图片添加倒影
	 * @param mContext
	 * @param imageId
	 * @return
	 */
	public ImageView createReflectedImages(Context mContext,int imageId) {
		Bitmap originalImage = BitmapFactory.decodeResource(mContext.getResources(), imageId);
		final int reflectionGap = 0;
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
				height / 2, width, height / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 2), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(originalImage, 0, 0, null);

		Paint deafaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, originalImage
				.getHeight(), 0, bitmapWithReflection.getHeight()
				+ reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.MIRROR);

		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		ImageView imageView = new ImageView(mContext);
		imageView.setImageBitmap(bitmapWithReflection);

		return imageView;
	}
	
	
}
