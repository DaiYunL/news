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
	private boolean ifContinuous=true;//�Ƿ�֧�������Խ���ģʽ
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
	 * �ж��ֻ�Ӳ���Ƿ�֧�ֱ佹����
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
	 * �佹�Ŵ�Zoom on
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
				Log.i(TAG,"��ǰ�ֻ��Ľ���ֵ��"+zoomValue);
				zoomValue += 3;
				if(zoomValue<=MAX) {
					mParams.setZoom(zoomValue);
					mCamera.setParameters(mParams);
				}
			}
			catch (Exception e)
			{
				Log.i(TAG,"�佹�����׳��쳣");
				e.printStackTrace();
			}
		}
		return zoomValue;
	}

	/**
	 * �佹��С ZoomIn
	 */
	public int ZoomIn()
	{
		int zoomValue=0;
		if (SupportZoom())
		{
			try
			{
				zoomValue = mParams.getZoom();
				Log.i(TAG,"��ǰ�ֻ��Ľ���ֵ��"+zoomValue);
				zoomValue -= 3;
				if(zoomValue>=0) {
					mParams.setZoom(zoomValue);
					mCamera.setParameters(mParams);
				}
			}
			catch (Exception e)
			{
				Log.i(TAG,"�佹�����׳��쳣");
				e.printStackTrace();
			}
		}
		return zoomValue;
	}
	/**��Camera
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
	/**ʹ��Surfaceview����Ԥ��
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
	 * ֹͣԤ�����ͷ�Camera
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
	 * ����
	 */
	int DST_RECT_WIDTH, DST_RECT_HEIGHT;
	public void doTakePicture(int w, int h){
		if(isPreviewing && (mCamera != null)){
			Log.i(TAG, "�������ճߴ�:width = " + w + " h = " + h);
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
			mParams.setPictureFormat(PixelFormat.JPEG);//�������պ�洢��ͼƬ��ʽ
			CamParaUtil.getInstance().printSupportPictureSize(mParams);
			CamParaUtil.getInstance().printSupportPreviewSize(mParams);
			//����PreviewSize��PictureSize
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
			mCamera.startPreview();//����Ԥ��
			mCamera.cancelAutoFocus();


			isPreviewing = true;
			mPreviwRate = previewRate;

			mParams = mCamera.getParameters(); //����getһ��
			Log.i(TAG, "��������:PreviewSize--With = " + mParams.getPreviewSize().width
					+ "Height = " + mParams.getPreviewSize().height);
			Log.i(TAG, "��������:PictureSize--With = " + mParams.getPictureSize().width
					+ "Height = " + mParams.getPictureSize().height);
		}
	}



	/*Ϊ��ʵ�����յĿ������������ձ�����Ƭ��Ҫ���������ص�����*/
	ShutterCallback mShutterCallback = new ShutterCallback() 
	//���Ű��µĻص������������ǿ����������Ʋ��š����ꡱ��֮��Ĳ�����Ĭ�ϵľ������ꡣ
	{
		public void onShutter() {
			// TODO Auto-generated method stub
			Log.i(TAG, "myShutterCallback:onShutter...");
		}
	};
	/**
	 * ��������
	 */
	PictureCallback mJpegPictureCallback = new PictureCallback()
	//��jpegͼ�����ݵĻص�,����Ҫ��һ���ص�
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if(null != data){
				b = BitmapFactory.decodeByteArray(data, 0, data.length);//data���ֽ����ݣ����������λͼ
				mCamera.stopPreview();
				isPreviewing = false;
			}
			//����ͼƬ��sdcard
			if(null != b)
			{
				//����FOCUS_MODE_CONTINUOUS_VIDEO)֮��myParam.set("rotation", 90)ʧЧ��
				//ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��
				Bitmap rotaBitmap;
					rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);//��������תͼƬ
				FileUtil.saveBitmap(rotaBitmap,handler);
			}
			//�ٴν���Ԥ��
			mCamera.startPreview();
			isPreviewing = true;
		}
	};

	/**
	 * ����ָ�������Rect
	 */
	PictureCallback mRectJpegPictureCallback = new PictureCallback() 
	//��jpegͼ�����ݵĻص�,����Ҫ��һ���ص�
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if(null != data){
				b = BitmapFactory.decodeByteArray(data, 0, data.length);//data���ֽ����ݣ����������λͼ
				mCamera.stopPreview();
				isPreviewing = false;
			}
			//����ͼƬ��sdcard
			if(null != b)
			{
				//����FOCUS_MODE_CONTINUOUS_VIDEO)֮��myParam.set("rotation", 90)ʧЧ��
				//ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��
				Bitmap rotaBitmap;
				if(CameraActivity.orientation==true) {
					rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);//��������תͼƬ
				}
				else{
					rotaBitmap=ImageUtil.getRotateBitmap(b,0f);//����ʱ����תͼƬ
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
			//�ٴν���Ԥ��
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
	// ���Զ��Խ�ʱ�����÷���
	@Override
	public void onAutoFocus(boolean success, Camera camera)
	{
		if (success)
		{
			// takePicture()������Ҫ����3������������
			// ��1�������������û����¿���ʱ�����ü�����
			// ��2�����������������ȡԭʼ��Ƭʱ�����ü�����
			// ��3�����������������ȡJPG��Ƭʱ�����ü�����
			camera.takePicture(new ShutterCallback()
			{
				public void onShutter()
				{
					// ���¿���˲���ִ�д˴�����
				}
			}, null, mRectJpegPictureCallback);  //��
		}
	}
};

}
