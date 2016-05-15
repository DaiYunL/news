package com.wendy.work.camera1.camera.preview;

import com.wendy.work.camera1.activity.CameraActivity;
import com.wendy.work.camera1.camera.CameraInterface;
import com.wendy.work.camera1.util.ResultShowActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera.CameraInfo;
import android.view.ViewGroup;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Wendy";
	Context mContext;
	SurfaceHolder mSurfaceHolder;
	CameraActivity mCameraActivity;
	public static  Handler handler;
	public void getActivity(CameraActivity cameraActivity)
	{
		this.mCameraActivity=cameraActivity;
		handler=new  Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==0x11)
				{
					String s=msg.getData().getString("message");
					//showToast(s);
					Intent intent=new Intent(mCameraActivity,ResultShowActivity.class);
					Bundle bundle=new Bundle();
					bundle.putString("result",s);
					intent.putExtras(bundle);
					getContext().startActivity(intent);
				}
			}
		};
	}
	public CameraSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);


		// TODO Auto-generated constructor stub
		mContext = context;
		mSurfaceHolder = getHolder();
		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent°ëÍ¸Ã÷ transparentÍ¸Ã÷
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceHolder.setKeepScreenOn(true);
		mSurfaceHolder.addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.i(TAG, "surfaceCreated...");
		mCameraActivity.cameraHasOpened();

		CameraInterface.getInstance().doOpenCamera(null, CameraInfo.CAMERA_FACING_BACK, handler);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "surfaceChanged...");
		CameraActivity activity=(CameraActivity)getContext();
		CameraInterface.getInstance().doStartPreview(mSurfaceHolder, 1.333f,activity.getZoomValue());
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.i(TAG, "surfaceDestroyed...");
		CameraInterface.getInstance().doStopCamera();
	}
	public SurfaceHolder getSurfaceHolder(){
		return mSurfaceHolder;
	}
	
}
