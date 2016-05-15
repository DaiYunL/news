package com.wendy.work.camera1.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;

public class DisplayUtil {
	private static final String TAG = "Wendy";
	/**
	 * 判断手机拍摄时的方向，竖屏为1，横屏向左为0，横屏向右暂时不考虑
	 */
	public static boolean judgeOrientation(float gravityX,float gravityY){
		if(Math.abs(gravityX-gravityY)<2)
			return true;
		else{
			if(gravityX-gravityY>0)
				return false;
			else
				return true;
		}
	}
	/** * 返回当前屏幕是否为竖屏。
	 * @param context
	 * @return 当且仅当当前屏幕为竖屏时返回true,否则返回false。
	 **/
	public static boolean isScreenOriatationPortrait(Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
	/**
	 * dip转px
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context context, float dipValue){            
		final float scale = context.getResources().getDisplayMetrics().density;                 
		return (int)(dipValue * scale + 0.5f);         
	}     
	
	/**
	 * px转dip
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue){                
		final float scale = context.getResources().getDisplayMetrics().density;                 
		return (int)(pxValue / scale + 0.5f);         
	} 
	
	/**
	 * 获取屏幕宽度和高度，单位为px
	 * @param context
	 * @return
	 */
	public static Point getScreenMetrics(Context context){
		DisplayMetrics dm =context.getResources().getDisplayMetrics();
		int w_screen = dm.widthPixels;
		int h_screen = dm.heightPixels;
		Log.i(TAG, "Screen---Width = " + w_screen + " Height = " + h_screen + " densityDpi = " + dm.densityDpi);
		return new Point(w_screen, h_screen);
		
	}
	
	/**
	 * 获取屏幕长宽比
	 * @param context
	 * @return
	 */
	public static float getScreenRate(Context context){
		Point P = getScreenMetrics(context);
		float H = P.y;
		float W = P.x;
		return (H/W);
	}
}
