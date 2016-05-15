package com.wendy.work.camera1.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.wendy.work.camera1.activity.CameraActivity;
import com.wendy.work.camera1.util.CamParaUtil;
import com.wendy.work.camera1.util.FileUtil;
import com.wendy.work.camera1.util.ImageUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class CameraInterface {
	private static final String TAG = "YanZi";
	private Camera mCamera;
	private Camera.Parameters mParams;
	private boolean isPreviewing = false;
	private float mPreviwRate = -1f;
	private int mCameraId = -1;
	private static CameraInterface mCameraInterface;
	public Handler handler;
	private boolean ifContinuous=true;//是否支持连续对焦的模式
	private boolean isSupportZoom=false;

	public interface CamOpenOverCallback{
		public void cameraHasOpened();
	}

	private CameraInterface(){
	}
	public static synchronized CameraInterface getInstance(){
		if(mCameraInterface == null){
			mCameraInterface = new CameraInterface();
		}
		return mCameraInterface;
	}

	/**
	 * 判断手机硬件是否支持变焦功能
	 * @return
	 */
	public boolean SupportZoom()
	{
		if (mCamera.getParameters().isZoomSupported())
		{
			isSupportZoom=true;
		}
		return isSupportZoom;
	}

	/**
	 * 变焦放大Zoom on
	 */
	public int ZoomOn()
	{
		int zoomValue=0;
		if (SupportZoom())
			{
				try
				{
				final int MAX = mParams.getMaxZoom();
				zoomValue = mParams.getZoom();
				Log.i(TAG,"当前手机的焦距值是"+zoomValue);
				zoomValue += 3;
				if(zoomValue<=MAX) {
					mParams.setZoom(zoomValue);
					mCamera.setParameters(mParams);
				}
			}
			catch (Exception e)
			{
				Log.i(TAG,"变焦过程抛出异常");
				e.printStackTrace();
			}
		}
		return zoomValue;
	}

	/**
	 * 变焦缩小 ZoomIn
	 */
	public int ZoomIn()
	{
		int zoomValue=0;
		if (SupportZoom())
		{
			try
			{
				zoomValue = mParams.getZoom();
				Log.i(TAG,"当前手机的焦距值是"+zoomValue);
				zoomValue -= 3;
				if(zoomValue>=0) {
					mParams.setZoom(zoomValue);
					mCamera.setParameters(mParams);
				}
			}
			catch (Exception e)
			{
				Log.i(TAG,"变焦过程抛出异常");
				e.printStackTrace();
			}
		}
		return zoomValue;
	}
	/**打开Camera
	 * @param callback
	 */
	public void doOpenCamera(CamOpenOverCallback callback,int cameraId,Handler handler){
		this.handler=handler;
		Log.i(TAG, "Camera open....");
		if(mCamera==null) {
			mCamera = Camera.open(cameraId);
			mCameraId = cameraId;
			if (callback != null) {
				callback.cameraHasOpened();
			}
		}
	}
	/**使用Surfaceview开启预览
	 * @param holder
	 * @param previewRate
	 */
	public void doStartPreview(SurfaceHolder holder, float previewRate,int zoomValue){
		Log.i(TAG, "doStartPreview...");
		if(isPreviewing){
			mCamera.stopPreview();
			return;
		}
		if(mCamera != null){
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initCamera(previewRate,zoomValue);
		}


			}

	/**
	 * 停止预览，释放Camera
	 */
	public void doStopCamera(){
		if(null != mCamera)
		{
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview(); 
			isPreviewing = false; 
			mPreviwRate = -1f;
			mCamera.release();
			mCamera = null;     
		}
	}
	/**
	 * 拍照
	 */
	int DST_RECT_WIDTH, DST_RECT_HEIGHT;
	public void doTakePicture(int w, int h){
		if(isPreviewing && (mCamera != null)){
			Log.i(TAG, "矩形拍照尺寸:width = " + w + " h = " + h);
			DST_RECT_WIDTH = w;
			DST_RECT_HEIGHT = h;
			if(ifContinuous) {
				mCamera.takePicture(mShutterCallback, null, mRectJpegPictureCallback);
			}
			else
			{
				mCamera.autoFocus(autoFocusCallback);
			}
		}
	}
	public Point doGetPrictureSize(){
		Size s = mCamera.getParameters().getPictureSize();
		return new Point(s.width, s.height);
	}





	private void initCamera(float previewRate,int zoomValue){
		if(mCamera != null){
			mParams = mCamera.getParameters();
			mParams.setZoom(zoomValue);
			mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
			CamParaUtil.getInstance().printSupportPictureSize(mParams);
			CamParaUtil.getInstance().printSupportPreviewSize(mParams);
			//设置PreviewSize和PictureSize
			Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
					mParams.getSupportedPictureSizes(),previewRate, 800);
			mParams.setPictureSize(pictureSize.width, pictureSize.height);
			Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
					mParams.getSupportedPreviewSizes(), previewRate, 800);
			mParams.setPreviewSize(previewSize.width, previewSize.height);

			mCamera.setDisplayOrientation(90);

//			CamParaUtil.getInstance().printSupportFocusMode(mParams);
			List<String> focusModes = mParams.getSupportedFocusModes();
			if(focusModes.contains("continuous-video")){
				mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			}
			else
			{
				ifContinuous=false;
			}
			mCamera.setParameters(mParams);	
			mCamera.startPreview();//开启预览
			mCamera.cancelAutoFocus();


			isPreviewing = true;
			mPreviwRate = previewRate;

			mParams = mCamera.getParameters(); //重新get一次
			Log.i(TAG, "最终设置:PreviewSize--With = " + mParams.getPreviewSize().width
					+ "Height = " + mParams.getPreviewSize().height);
			Log.i(TAG, "最终设置:PictureSize--With = " + mParams.getPictureSize().width
					+ "Height = " + mParams.getPictureSize().height);
		}
	}



	/*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
	ShutterCallback mShutterCallback = new ShutterCallback() 
	//快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
	{
		public void onShutter() {
			// TODO Auto-generated method stub
			Log.i(TAG, "myShutterCallback:onShutter...");
		}
	};
	/**
	 * 常规拍照
	 */
	PictureCallback mJpegPictureCallback = new PictureCallback()
	//对jpeg图像数据的回调,最重要的一个回调
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if(null != data){
				b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
				mCamera.stopPreview();
				isPreviewing = false;
			}
			//保存图片到sdcard
			if(null != b)
			{
				//设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
				//图片竟然不能旋转了，故这里要旋转下
				Bitmap rotaBitmap;
					rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);//竖屏是旋转图片
				FileUtil.saveBitmap(rotaBitmap,handler);
			}
			//再次进入预览
			mCamera.startPreview();
			isPreviewing = true;
		}
	};

	/**
	 * 拍摄指定区域的Rect
	 */
	PictureCallback mRectJpegPictureCallback = new PictureCallback() 
	//对jpeg图像数据的回调,最重要的一个回调
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if(null != data){
				b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
				mCamera.stopPreview();
				isPreviewing = false;
			}
			//保存图片到sdcard
			if(null != b)
			{
				//设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
				//图片竟然不能旋转了，故这里要旋转下
				Bitmap rotaBitmap;
				if(CameraActivity.orientation==true) {
					rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);//竖屏是旋转图片
				}
				else{
					rotaBitmap=ImageUtil.getRotateBitmap(b,0f);//横屏时不旋转图片
				}
				int x = rotaBitmap.getWidth()/2 - DST_RECT_WIDTH/2;
				int y = rotaBitmap.getHeight()/2 - DST_RECT_HEIGHT/2;
				Log.i(TAG, "rotaBitmap.getWidth() = " + rotaBitmap.getWidth()
						+ " rotaBitmap.getHeight() = " + rotaBitmap.getHeight());
				Bitmap rectBitmap = Bitmap.createBitmap(rotaBitmap, x, y, DST_RECT_WIDTH, DST_RECT_HEIGHT);
				FileUtil.saveBitmap(rectBitmap,handler);
				if(rotaBitmap.isRecycled()){
					rotaBitmap.recycle();
					rotaBitmap = null;
				}
				if(rectBitmap.isRecycled()){
					rectBitmap.recycle();
					rectBitmap = null;
				}
			}
			//再次进入预览
			mCamera.startPreview();
			isPreviewing = true;
			if(!b.isRecycled()){
				b.recycle();
				b = null;
			}

		}
	};
Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback()
{
	// 当自动对焦时激发该方法
	@Override
	public void onAutoFocus(boolean success, Camera camera)
	{
		if (success)
		{
			// takePicture()方法需要传入3个监听器参数
			// 第1个监听器：当用户按下快门时激发该监听器
			// 第2个监听器：当相机获取原始照片时激发该监听器
			// 第3个监听器：当相机获取JPG照片时激发该监听器
			camera.takePicture(new ShutterCallback()
			{
				public void onShutter()
				{
					// 按下快门瞬间会执行此处代码
				}
			}, null, mRectJpegPictureCallback);  //⑤
		}
	}
};

}
